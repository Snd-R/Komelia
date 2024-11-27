package snd.webview

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebMessage
import android.webkit.WebMessagePort
import android.webkit.WebMessagePort.WebMessageCallback
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.DataNode
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.parseInputStream
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import snd.webview.WebviewCallback.CallbackResponse
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass
import kotlin.reflect.typeOf


val logger = KotlinLogging.logger {}

@SuppressLint("SetJavaScriptEnabled")
actual class KomeliaWebview(private val webview: WebView) : WebViewClient(), AutoCloseable {
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    val mainDispatcherScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val isRunning = AtomicBoolean(false)
    private var currentUrl: Uri? = null
    private val bindFunctions = mutableMapOf<String, WebviewCallback>()
    private var interceptor: ResourceLoadHandler? = null
    private var incomingPort: WebMessagePort? = null
    private var outgoingPort: WebMessagePort? = null

    init {
        webview.settings.javaScriptEnabled = true
        webview.settings.mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW
        webview.settings.domStorageEnabled = true
        webview.settings.allowFileAccess = true
        webview.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        webview.settings.setSupportZoom(true)
        webview.settings.textZoom = 100
        webview.webViewClient = this
        logger.info { webview.settings.userAgentString }

        //TODO look into WebViewCompat.addWebMessageListener() and WebViewCompat.addDocumentStartJavaScript()
        // should be possible to inject js without modifying original html document
//        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
//            WebViewCompat.addDocumentStartJavaScript(webview, initScript, setOf("*"))
//        }
    }

    actual suspend inline fun <reified JsArgs, reified Result> bind(
        name: String,
        function: JsCallback<JsArgs, Result>
    ) {
        bind(name) { id, jsRequest ->
            mainDispatcherScope.launch {
                runCatching {
                    val argsClass = typeOf<JsArgs>().classifier as KClass<*>
                    val resultClass = typeOf<Result>().classifier as KClass<*>

                    val arguments = json.decodeFromString<List<JsArgs>>(jsRequest)
                    val argument = if (argsClass == Unit::class) {
                        Unit as JsArgs
                    } else {
                        arguments[0]
                    }

                    val result = function.run(argument)

                    if (resultClass == Unit::class) {
                        bindReturn(id, json.encodeToString(CallbackResponse("")))
                    } else {
                        val json = json.encodeToString<CallbackResponse<Result>>(CallbackResponse(result))
                        bindReturn(id, json)
                    }
                }.onFailure { error ->
                    logger.error(error) { "Encountered error during execution of bind function \"$name\"; js params: $jsRequest" }
                    val message = json.encodeToString<CallbackResponse<String>>(
                        CallbackResponse(error.message ?: error.stackTraceToString())
                    )
                    bindReject(id, message)
                }
            }
        }
    }

    actual fun navigate(uri: String) {
        mainDispatcherScope.launch {
            currentUrl = Uri.parse(uri)
            if (isRunning.get()) {
                webview.loadUrl(uri)
            }
        }
    }

    actual fun registerRequestInterceptor(handler: ResourceLoadHandler) {
        mainDispatcherScope.launch {
            interceptor = handler
        }
    }

    actual fun start() {
        mainDispatcherScope.launch {
            if (!isRunning.compareAndSet(false, true)) return@launch
            currentUrl?.let {
                webview.loadUrl(it.toString())
            }
        }
    }

    override fun close() {}

    suspend fun bind(name: String, callback: WebviewCallback) {
        withContext(Dispatchers.Main) {
            bindFunctions[name] = callback

            webview.evaluateJavascript(
                """
                  if (window.__webview__) {
                    window.__webview__.onBind(${json.encodeToString(name)});
                  }
                """,
                null
            )
        }
    }

    private suspend fun resolve(id: String, status: Int, result: String) {
        withContext(Dispatchers.Main) {
            webview.evaluateJavascript(
                """
              window.__webview__.onReply(
                ${json.encodeToString(id)}, $status, ${json.encodeToString(result)}
              )
            """,
                null
            )
        }
    }

    suspend fun bindReturn(id: String, result: String) {
        resolve(id, 0, result)
    }

    suspend fun bindReject(id: String, message: String) {
        resolve(id, -1, message)
    }

    // TODO non blocking
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val response = this.interceptor?.run(request.url.toString()) ?: return null

        if (currentUrl == request.url) {
            val htmlDocument = Ksoup.parseInputStream(response.data.inputStream(), "")

            val bindScriptElement = Element("script")
            bindScriptElement.appendChild(DataNode(createBindScript()))
            htmlDocument.head().prependChild(bindScriptElement)

            val initScriptElement = Element("script")
            initScriptElement.appendChild(DataNode(initScript))
            htmlDocument.head().prependChild(initScriptElement)

            return WebResourceResponse(
                "text/html",
                "utf-8",
                htmlDocument.outerHtml().byteInputStream(Charsets.UTF_8)
            )
        }

        return WebResourceResponse(response.contentType, null, response.data.inputStream())
    }

    override fun onPageFinished(view: WebView, url: String) {
        val channel = view.createWebMessageChannel()
        this.incomingPort = channel[0]
        this.outgoingPort = channel[1]
        channel[0].setWebMessageCallback(object : WebMessageCallback() {
            override fun onMessage(port: WebMessagePort, message: WebMessage) {
                val webMessage = json.decodeFromString<WebviewMessage>(message.data)
                val callback = bindFunctions[webMessage.method]
                if (callback == null) {
                    mainDispatcherScope.launch {
                        bindReject(webMessage.id, "Function not found")
                    }
                    return
                }
                callback.run(webMessage.id, webMessage.params.toString())
            }
        })
        view.postWebMessage(WebMessage("", arrayOf(outgoingPort)), requireNotNull(currentUrl))
    }


    private fun createBindScript(): String {
        val jsNames = json.encodeToString(bindFunctions.keys)
        return """
                'use strict';
                var methods = $jsNames;
                methods.forEach(function(name){
                  window.__webview__.onBind(name);
                })
            """
    }
}

actual fun webviewIsAvailable() = true

// adapted from https://github.com/webview/webview/blob/1e1298331e687e23871a61854a016df45c8e419c/core/include/webview/detail/engine_base.hh#L203
private const val initScript = """
(function() {
  'use strict';
  var port;
  var initQueue = [];
  window.onmessage = function(e) {
    port = e.ports[0];
    initQueue.forEach((el) => port.postMessage(JSON.stringify(el)));
    initQueue = [];
    window.onmessage = undefined;
  }
  
  function generateId() {
    var crypto = window.crypto || window.msCrypto;
    var bytes = new Uint8Array(16);
    crypto.getRandomValues(bytes);
    return Array.prototype.slice.call(bytes).map(function(n) {
      var s = n.toString(16);
      return ((s.length % 2) == 1 ? '0' : '') + s;
    }).join('');
  }
  var Webview = (function() {
    var _promises = {};
    function Webview_() {}
    Webview_.prototype.call = function(method) {
      var _id = generateId();
      var _params = Array.prototype.slice.call(arguments, 1);
      var promise = new Promise(function(resolve, reject) {
        _promises[_id] = { resolve, reject };
      });
      var message = { id: _id, method: method, params: _params};
      if(port == undefined){
        initQueue.push(message);
      } else{
        port.postMessage(JSON.stringify(message))
      }
      return promise;
    };
    Webview_.prototype.onReply = function(id, status, result) {
      var promise = _promises[id];
      if (result !== undefined) {
        try {
          result = JSON.parse(result);
        } catch (e) {
          promise.reject(new Error("Failed to parse binding result as JSON"));
          return;
        }
      }
      if (status === 0) {
        promise.resolve(result);
      } else {
        promise.reject(result);
      }
    };
    Webview_.prototype.onBind = function(name) {
      if (window.hasOwnProperty(name)) {
        throw new Error('Property "' + name + '" already exists');
      }
      window[name] = (function() {
        var params = [name].concat(Array.prototype.slice.call(arguments));
        return Webview_.prototype.call.apply(this, params);
      }).bind(this);
    };
    Webview_.prototype.onUnbind = function(name) {
      if (!window.hasOwnProperty(name)) {
        throw new Error('Property "' + name + '" does not exist');
      }
      delete window[name];
    };
    return Webview_;
  })();
  window.__webview__ = new Webview();
})(); 
"""

fun interface WebviewCallback {
    fun run(id: String, request: String)

    @Serializable
    data class CallbackResponse<T>(
        val result: T
    )
}

@Serializable
data class WebviewMessage(
    val id: String,
    val method: String,
    val params: JsonArray
)

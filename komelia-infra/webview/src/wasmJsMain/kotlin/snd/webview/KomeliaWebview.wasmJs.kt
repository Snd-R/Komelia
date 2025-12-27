package snd.webview

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.DataNode
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.http.*
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.MessageChannel
import org.w3c.dom.MessageEvent
import org.w3c.dom.Window
import snd.webview.WebviewCallback.CallbackResponse
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

actual class KomeliaWebview : AutoCloseable {
    private val bindFunctions = mutableMapOf<String, WebviewCallback>()
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    var isActive = false
        private set
    private var currentUrl: String? = null
    private var requestInterceptor: RequestInterceptor? = null
    val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var webviewIframe: HTMLIFrameElement? = null
    private var channel: MessageChannel? = null

    actual suspend inline fun <reified JsArgs, reified Result> bind(
        name: String,
        function: JsCallback<JsArgs, Result>
    ) {
        bind(name) { id, jsRequest ->
            coroutineScope.launch {
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
                    val message = json.encodeToString<CallbackResponse<String>>(
                        CallbackResponse(error.message ?: error.stackTraceToString())
                    )
                    bindReject(id, message)
                }
            }
        }
    }

    actual fun start() {
        if (isActive) return
        isActive = true
        currentUrl?.let { navigate(it) }
    }

    actual fun navigate(uri: String) {
        val url = Url(uri)
        currentUrl = uri

        if (isActive) {
            val iframe = document.createElement("iframe") as HTMLIFrameElement
            iframe.setAttribute(
                "style",
                "position:fixed; top:0; left:0; bottom:0; right:0; width:100%; height:100%; border:none; margin:0; padding:0; overflow:scroll; z-index:999999"
            )
            val channel = MessageChannel().also { this.channel = it }
            channel.port1.onmessage = this::onPortMessage
            iframe.addEventListener("load") {
                iframe.contentWindow!!.postMessage(null, "*", singleElementJsArray(channel.port2))
            }
            webviewIframe = iframe

            coroutineScope.launch {
                val response = requestInterceptor?.run(
                    ResourceRequest(
                        url,
                        HttpMethod.Get,
                        Headers.Empty
                    )
                ) ?: error("Can't get $uri response")

                if (response.contentType != "text/html") error("Can't navigate to non html page")
                val htmlString = response.data.decodeToString()
                val htmlDocument = Ksoup.parse(htmlString)

                val bindScriptElement = Element("script")
                bindScriptElement.appendChild(DataNode(createBindScript()))
                htmlDocument.head().prependChild(bindScriptElement)

                val initScriptElement = Element("script")
                initScriptElement.appendChild(DataNode(initScript))
                htmlDocument.head().prependChild(initScriptElement)

                iframe.srcdoc = htmlDocument.outerHtml()
                document.body!!.appendChild(iframe)
            }
        }
    }

    actual fun registerRequestInterceptor(handler: RequestInterceptor) {
        this.requestInterceptor = handler
    }

    override fun close() {
        webviewIframe?.let { document.body!!.removeChild(it) }
    }

    fun bind(name: String, callback: WebviewCallback) {
        bindFunctions[name] = callback
        if (webviewIframe != null && isActive) {
            evalJs(
                window = requireNotNull(webviewIframe?.contentWindow),
                js = """
                      if (window.__webview__) {
                        window.__webview__.onBind(${json.encodeToString(name)});
                      }
                   """,
            )
        }
    }

    private fun resolve(id: String, status: Int, result: String) {
        evalJs(
            window = requireNotNull(webviewIframe?.contentWindow),
            js = "window.__webview__.onReply(${json.encodeToString(id)}, $status, ${json.encodeToString(result)})"
        )
    }

    fun bindReturn(id: String, result: String) {
        resolve(id, 0, result)
    }

    fun bindReject(id: String, message: String) {
        resolve(id, -1, message)
    }

    private fun onPortMessage(message: MessageEvent) {
        val messageData = json.decodeFromString<WebviewMessage>(requireNotNull(message.data).toString())
        val callback = bindFunctions[messageData.method]
        if (callback == null) {
            bindReject(messageData.id, "Function with name ${messageData.method} is not found")
            return
        }
        callback.run(messageData.id, messageData.params.toString())
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
    data class CallbackResponse<T>(val result: T)
}

@Serializable
data class WebviewMessage(
    val id: String,
    val method: String,
    val params: JsonArray
)

internal fun singleElementJsArray(element: JsAny): JsArray<JsAny> {
    js("return [element];")
}

internal fun evalJs(window: Window, js: String) {
    js("window.eval(js);")
}

internal fun jsArrayAsJson(array: JsArray<JsAny>): String {
    js("return JSON.stringify(array);")
}

actual fun webviewIsAvailable() = true

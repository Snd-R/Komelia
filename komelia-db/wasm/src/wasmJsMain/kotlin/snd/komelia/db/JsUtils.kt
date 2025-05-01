package snd.komelia.db

internal fun <T : JsAny> makeJsObject(): T = js("{ return {}; }")

@Suppress("UNUSED_PARAMETER")
private fun getObjectField(obj: JsAny, name: String): JsAny = js("obj[name]")

@Suppress("UNUSED_PARAMETER")
private fun setObjectField(obj: JsAny, name: String, value: JsAny): Unit = js("obj[name]=value")

internal operator fun JsAny.get(name: String): JsAny =
    getObjectField(this, name)

internal operator fun JsAny.set(name: String, value: JsAny) =
    setObjectField(this, name, value)

internal operator fun JsAny.set(name: String, value: String) =
    setObjectField(this, name, value.toJsString())

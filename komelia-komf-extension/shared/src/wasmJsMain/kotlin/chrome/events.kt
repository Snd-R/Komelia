@file:JsQualifier("chrome.events")

package chrome.events

external interface Event : JsAny {
    fun addListener(callback: (JsAny) -> Unit)
    fun getRules(callback: (rules: JsArray<Rule>) -> Unit)
    fun getRules(ruleIdentifiers: JsArray<JsString>, callback: (rules: JsArray<Rule>) -> Unit)
    fun hasListener(callback: (JsAny) -> Unit): Boolean
    fun removeRules(
        ruleIdentifiers: JsArray<JsString>? = definedExternally /* null */,
        callback: (() -> Unit)? = definedExternally /* null */
    )

    fun removeRules(callback: (() -> Unit)? = definedExternally /* null */)
    fun addRules(rules: JsArray<Rule>, callback: ((rules: JsArray<Rule>) -> Unit)? = definedExternally /* null */)
    fun removeListener(callback: (JsAny) -> Unit)
    fun hasListeners(): Boolean
    fun removeRules()
}

external interface Rule : JsAny {
    var priority: JsNumber? get() = definedExternally; set(value) = definedExternally
    var conditions: JsArray<JsAny>
    var id: JsString? get() = definedExternally; set(value) = definedExternally
    var actions: JsArray<JsAny>
    var tags: JsArray<JsString>? get() = definedExternally; set(value) = definedExternally
}
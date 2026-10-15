package chrome.tabs

fun TabsQuery(url: JsArray<JsString>): JsAny {
    js("return { url: url };")
}

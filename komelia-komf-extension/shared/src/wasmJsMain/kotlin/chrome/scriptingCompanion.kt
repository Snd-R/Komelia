package chrome.scripting

fun RegisteredContentScript(
    id: String,
    js: JsArray<JsString>? = null,
    css: JsArray<JsString>? = null,
    matches: JsArray<JsString>? = null,
    excludeMatches: JsArray<JsString>? = null,
    matchOriginAsFallback: Boolean? = null,
    persistAcrossSessions: Boolean? = null,
    allFrames: Boolean? = null,
    runAt: String? = null,
    world: String? = null,
): RegisteredContentScript {
    js(
        """
            return {
                id: id,
                js: js,
                css: css,
                matches: matches,
                excludeMatches: excludeMatches,
                matchOriginAsFallback: matchOriginAsFallback,
                persistAcrossSessions: persistAcrossSessions,
                allFrames: allFrames,
                runAt: runAt,
                world: world
           }; 
        """
    )
}

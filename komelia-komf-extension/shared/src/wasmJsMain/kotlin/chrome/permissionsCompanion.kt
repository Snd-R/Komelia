package chrome.permissions

fun Permissions(permissions: List<String>, origins: List<String>): Permissions {
    return Permissions(
        permissions.map { it.toJsString() }.toJsArray(),
        origins.map { it.toJsString() }.toJsArray(),
    )
}

fun Permissions(permissions: JsArray<JsString>, origins: JsArray<JsString>): Permissions {
    js("return { permissions: permissions, origins: origins};")
}

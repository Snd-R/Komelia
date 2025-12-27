#include <webkit2/webkit-web-extension.h>

void release_uri(gpointer data, GObject *) {
    if (data != NULL) g_free(data);
}

static gboolean send_request_callback(
    WebKitWebPage *self,
    WebKitURIRequest *request,
    WebKitURIResponse *redirected_response,
    gpointer user_data
) {
    const gchar *uri = webkit_uri_request_get_uri(request);

    if (strncmp("http", uri, 4) == 0) {
        char *new_uri = malloc(sizeof(char) * (strlen(uri) + 10));
        strcpy(new_uri, "komelia");
        strcat(new_uri, uri + 4);

        g_object_weak_ref((GObject *) request, release_uri, new_uri);
        webkit_uri_request_set_uri(request, new_uri);
    }

    return FALSE;
}

static void web_page_created_callback(WebKitWebExtension *extension, WebKitWebPage *web_page, gpointer) {
    g_signal_connect(web_page, "send-request", G_CALLBACK (send_request_callback), NULL);
}


G_MODULE_EXPORT void webkit_web_extension_initialize(WebKitWebExtension *extension) {
    g_signal_connect(extension, "page-created", G_CALLBACK (web_page_created_callback), NULL);
}

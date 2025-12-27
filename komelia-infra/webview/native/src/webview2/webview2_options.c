#include "WebView2.h"
#include <wchar.h>
#include "webview2_options.h"

#include <stdio.h>
#include <glib.h>
#include <unistd.h>

ULONG static_AddRef(void *This) {
    return 1;
}

ULONG static_Release(void *This) {
    return 0;
}

static LPWSTR registration1_schemeName = L"komelia";
static LPWSTR registration2_schemeName = L"komeliaf";
static BOOL registration_treatAsSecure = 1;
static BOOL registration_hasAuthorityComponent = 0;

static GMutex mutex;

HRESULT registration_QueryInterface(
    ICoreWebView2CustomSchemeRegistration *This,
    REFIID riid,
    void **ppvObject
) {
    *ppvObject = This;
    return S_OK;
}

HRESULT registration_get_SchemeName1(ICoreWebView2CustomSchemeRegistration *This, LPWSTR *schemeName) {
    size_t len = wcslen(registration1_schemeName);
    *schemeName = CoTaskMemAlloc(len);
    wcscpy(*schemeName, registration1_schemeName);

    return S_OK;
}

HRESULT registration_get_SchemeName2(ICoreWebView2CustomSchemeRegistration *This, LPWSTR *schemeName) {
    size_t len = wcslen(registration2_schemeName);
    *schemeName = CoTaskMemAlloc(len);
    wcscpy(*schemeName, registration2_schemeName);

    return S_OK;
}

HRESULT registration_get_TreatAsSecure(ICoreWebView2CustomSchemeRegistration *This, BOOL *treatAsSecure) {
    *treatAsSecure = registration_treatAsSecure;
    return S_OK;
}

HRESULT registration_put_TreatAsSecure(ICoreWebView2CustomSchemeRegistration *This, BOOL value) {
    return S_OK;
}

HRESULT registration_GetAllowedOrigins(ICoreWebView2CustomSchemeRegistration *This, UINT32 *allowedOriginsCount,
                                       LPWSTR **allowedOrigins) {
    // *allowedOriginsCount = 1;
    // LPWSTR *origs = CoTaskMemAlloc(sizeof(void *) * 16);
    // LPWSTR allow_all = CoTaskMemAlloc(64);
    // allow_all[0] = L'*';
    // allow_all[1] = L'\0';
    // origs[0] = allow_all;
    // *allowedOrigins = origs;
    *allowedOriginsCount = 0;
    *allowedOrigins = NULL;

    return S_OK;
}

HRESULT registration_SetAllowedOrigins(ICoreWebView2CustomSchemeRegistration *This, UINT32 *allowedOriginsCount,
                                       LPCWSTR *allowedOrigins) {
    return S_OK;
}

HRESULT registration_get_HasAuthorityComponent(ICoreWebView2CustomSchemeRegistration *This,
                                               BOOL *hasAuthorityComponent) {
    *hasAuthorityComponent = registration_hasAuthorityComponent;
    return S_OK;
}

HRESULT registration_put_HasAuthorityComponent(ICoreWebView2CustomSchemeRegistration *This,
                                               BOOL hasAuthorityComponent) {
    return S_OK;
}

static ICoreWebView2CustomSchemeRegistrationVtbl scheme_registration1_vtbl = {
    registration_QueryInterface,
    static_AddRef,
    static_Release,
    registration_get_SchemeName1,
    registration_get_TreatAsSecure,
    registration_put_TreatAsSecure,
    registration_GetAllowedOrigins,
    registration_SetAllowedOrigins,
    registration_get_HasAuthorityComponent,
    registration_put_HasAuthorityComponent
};
static ICoreWebView2CustomSchemeRegistration scheme_registration1 = {&scheme_registration1_vtbl};

static ICoreWebView2CustomSchemeRegistrationVtbl scheme_registration2_vtbl = {
    registration_QueryInterface,
    static_AddRef,
    static_Release,
    registration_get_SchemeName2,
    registration_get_TreatAsSecure,
    registration_put_TreatAsSecure,
    registration_GetAllowedOrigins,
    registration_SetAllowedOrigins,
    registration_get_HasAuthorityComponent,
    registration_put_HasAuthorityComponent
};
static ICoreWebView2CustomSchemeRegistration scheme_registration2 = {&scheme_registration2_vtbl};

static ICoreWebView2CustomSchemeRegistration *registrations[2] = {&scheme_registration1, &scheme_registration2};
static LPCWSTR options_additionalBrowserArguments = L"";
static LPCWSTR options_language = L"en";
static LPCWSTR options_targetCompatibleBrowserVersion = L"130.0.2849.39";
static BOOL options_allowSingleSignOnUsingOSPrimaryAccount = 0;
static BOOL options_exclusiveUserDataFolderAccess = 0;
static BOOL options_isCustomCrashReportingEnabled = 0;
static BOOL options_enableTrackingPrevention = 0;
static BOOL options_areBrowserExtensionsEnabled = 1;

static ICoreWebView2EnvironmentOptions *options1_ptr = NULL;
static ICoreWebView2EnvironmentOptions2 *options2_ptr = NULL;
static ICoreWebView2EnvironmentOptions3 *options3_ptr = NULL;
static ICoreWebView2EnvironmentOptions4 *options4_ptr = NULL;
static ICoreWebView2EnvironmentOptions5 *options5_ptr = NULL;
static ICoreWebView2EnvironmentOptions6 *options6_ptr = NULL;
static ICoreWebView2EnvironmentOptions7 *options7_ptr = NULL;
static ICoreWebView2EnvironmentOptions8 *options8_ptr = NULL;

HRESULT options_QueryInterface(
    void *This,
    REFIID riid,
    void **ppvObject
) {
    g_mutex_lock(&mutex);
    if (IsEqualGUID(riid, &IID_ICoreWebView2EnvironmentOptions)) {
        *ppvObject = options1_ptr;
    } else if (IsEqualGUID(riid, &IID_ICoreWebView2EnvironmentOptions2)) {
        *ppvObject = options2_ptr;
    } else if (IsEqualGUID(riid, &IID_ICoreWebView2EnvironmentOptions3)) {
        *ppvObject = options3_ptr;
    } else if (IsEqualGUID(riid, &IID_ICoreWebView2EnvironmentOptions4)) {
        *ppvObject = options4_ptr;
    } else if (IsEqualGUID(riid, &IID_ICoreWebView2EnvironmentOptions5)) {
        *ppvObject = options5_ptr;
    } else if (IsEqualGUID(riid, &IID_ICoreWebView2EnvironmentOptions6)) {
        *ppvObject = options6_ptr;
    } else if (IsEqualGUID(riid, &IID_ICoreWebView2EnvironmentOptions7)) {
        *ppvObject = options7_ptr;
    } else if (IsEqualGUID(riid, &IID_ICoreWebView2EnvironmentOptions8)) {
        *ppvObject = options8_ptr;
    } else {
        LPOLESTR guidString;
        StringFromIID(riid, &guidString);
        g_mutex_unlock(&mutex);
        return E_NOINTERFACE;
    }
    g_mutex_unlock(&mutex);
    return S_OK;
}


HRESULT options_get_AdditionalBrowserArguments(ICoreWebView2EnvironmentOptions *This, LPWSTR *value) {
    // size_t len = wcslen(options_additionalBrowserArguments);
    // *value = CoTaskMemAlloc(len);
    // wcscpy(*value, options_additionalBrowserArguments);
    // return S_OK;
    *value = NULL;
    return S_OK;
}

HRESULT options_put_AdditionalBrowserArguments(ICoreWebView2EnvironmentOptions *This, LPCWSTR value) {
    return S_OK;
}

HRESULT options_get_Language(ICoreWebView2EnvironmentOptions *This, LPWSTR *value) {
    // size_t len = wcslen(options_language);
    // *value = CoTaskMemAlloc(len);
    // wcscpy(*value, options_language);
    // return S_OK;
    *value = NULL;
    return S_OK;
}

HRESULT options_put_Language(ICoreWebView2EnvironmentOptions *This, LPCWSTR value) {
    return S_OK;
}

HRESULT options_get_TargetCompatibleBrowserVersion(ICoreWebView2EnvironmentOptions *This, LPWSTR *value) {
    size_t len = wcslen(options_targetCompatibleBrowserVersion);
    *value = CoTaskMemAlloc(len);
    wcscpy(*value, options_targetCompatibleBrowserVersion);
    return S_OK;
}

HRESULT options_put_TargetCompatibleBrowserVersion(ICoreWebView2EnvironmentOptions *This, LPCWSTR value) {
    return S_OK;
}

HRESULT options_get_AllowSingleSignOnUsingOSPrimaryAccount(ICoreWebView2EnvironmentOptions *This, BOOL *allow) {
    *allow = options_allowSingleSignOnUsingOSPrimaryAccount;
    return S_OK;
}

HRESULT options_put_AllowSingleSignOnUsingOSPrimaryAccount(ICoreWebView2EnvironmentOptions *This, BOOL allow) {
    options_allowSingleSignOnUsingOSPrimaryAccount = allow;
    return S_OK;
}

HRESULT options_get_ExclusiveUserDataFolderAccess(ICoreWebView2EnvironmentOptions2 *This, BOOL *value) {
    *value = options_exclusiveUserDataFolderAccess;
    return S_OK;
}

HRESULT options_put_ExclusiveUserDataFolderAccess(ICoreWebView2EnvironmentOptions2 *This, BOOL value) {
    options_exclusiveUserDataFolderAccess = value;
    return S_OK;
}

HRESULT options_get_IsCustomCrashReportingEnabled(ICoreWebView2EnvironmentOptions3 *This, BOOL *value) {
    *value = options_isCustomCrashReportingEnabled;
    return S_OK;
}

HRESULT options_put_IsCustomCrashReportingEnabled(ICoreWebView2EnvironmentOptions3 *This, BOOL value) {
    options_isCustomCrashReportingEnabled = value;
    return S_OK;
}

HRESULT options_GetCustomSchemeRegistrations(ICoreWebView2EnvironmentOptions4 *This, UINT32 *count,
                                             ICoreWebView2CustomSchemeRegistration ***schemeRegistrations) {
    *count = 2;
    ICoreWebView2CustomSchemeRegistration **regs = CoTaskMemAlloc(sizeof(void *) * 2);
    regs[0] = &scheme_registration1;
    regs[1] = &scheme_registration2;
    *schemeRegistrations = regs;
    // *count = 0;
    // *schemeRegistrations = NULL;
    return S_OK;
}

HRESULT options_SetCustomSchemeRegistrations(ICoreWebView2EnvironmentOptions4 *This, UINT32 count,
                                             ICoreWebView2CustomSchemeRegistration **schemeRegistrations) {
    return S_OK;
}

HRESULT options_get_EnableTrackingPrevention(ICoreWebView2EnvironmentOptions5 *This, BOOL *value) {
    *value = options_enableTrackingPrevention;
    return S_OK;
}

HRESULT options_put_EnableTrackingPrevention(ICoreWebView2EnvironmentOptions5 *This, BOOL value) {
    options_enableTrackingPrevention = value;
    return S_OK;
}

HRESULT options_get_AreBrowserExtensionsEnabled(ICoreWebView2EnvironmentOptions6 *This, BOOL *value) {
    *value = options_areBrowserExtensionsEnabled;
    return S_OK;
}

HRESULT options_put_AreBrowserExtensionsEnabled(ICoreWebView2EnvironmentOptions6 *This, BOOL value) {
    options_areBrowserExtensionsEnabled = value;
    return S_OK;
}

HRESULT options_get_ChannelSearchKind(ICoreWebView2EnvironmentOptions7 *This, COREWEBVIEW2_CHANNEL_SEARCH_KIND *value) {
    *value = COREWEBVIEW2_CHANNEL_SEARCH_KIND_MOST_STABLE;
    return S_OK;
}

HRESULT options_put_ChannelSearchKind(ICoreWebView2EnvironmentOptions7 *This, BOOL value) {
    return S_OK;
}

HRESULT options_get_ReleaseChannels(ICoreWebView2EnvironmentOptions7 *This, COREWEBVIEW2_RELEASE_CHANNELS *value) {
    *value = COREWEBVIEW2_RELEASE_CHANNELS_STABLE | COREWEBVIEW2_RELEASE_CHANNELS_BETA |
             COREWEBVIEW2_RELEASE_CHANNELS_DEV | COREWEBVIEW2_RELEASE_CHANNELS_CANARY;
    return S_OK;
}

HRESULT options_put_ReleaseChannels(ICoreWebView2EnvironmentOptions7 *This, COREWEBVIEW2_RELEASE_CHANNELS value) {
    return S_OK;
}

HRESULT options_get_ScrollBarStyle(ICoreWebView2EnvironmentOptions8 *This, COREWEBVIEW2_SCROLLBAR_STYLE *value) {
    *value = COREWEBVIEW2_SCROLLBAR_STYLE_DEFAULT;
    return S_OK;
}

HRESULT options_put_ScrollBarStyle(ICoreWebView2EnvironmentOptions8 *This, COREWEBVIEW2_SCROLLBAR_STYLE value) {
    return S_OK;
}

static ICoreWebView2EnvironmentOptionsVtbl options1_vtbl = {
    options_QueryInterface,
    static_AddRef,
    static_Release,
    options_get_AdditionalBrowserArguments,
    options_put_AdditionalBrowserArguments,
    options_get_Language,
    options_put_Language,
    options_get_TargetCompatibleBrowserVersion,
    options_put_TargetCompatibleBrowserVersion,
    options_get_AllowSingleSignOnUsingOSPrimaryAccount,
    options_put_AllowSingleSignOnUsingOSPrimaryAccount,
};
static ICoreWebView2EnvironmentOptions options1 = {&options1_vtbl};

static ICoreWebView2EnvironmentOptions2Vtbl options2_vtbl = {
    options_QueryInterface,
    static_AddRef,
    static_Release,
    options_get_ExclusiveUserDataFolderAccess,
    options_put_ExclusiveUserDataFolderAccess,
};
static ICoreWebView2EnvironmentOptions2 options2 = {&options2_vtbl};

static ICoreWebView2EnvironmentOptions3Vtbl options3_vtbl = {
    options_QueryInterface,
    static_AddRef,
    static_Release,
    options_get_IsCustomCrashReportingEnabled,
    options_put_IsCustomCrashReportingEnabled,
};
static ICoreWebView2EnvironmentOptions3 options3 = {&options3_vtbl};

static ICoreWebView2EnvironmentOptions4Vtbl options4_vtbl = {
    options_QueryInterface,
    static_AddRef,
    static_Release,
    options_GetCustomSchemeRegistrations,
    options_SetCustomSchemeRegistrations,
};
static ICoreWebView2EnvironmentOptions4 options4 = {&options4_vtbl};

static ICoreWebView2EnvironmentOptions5Vtbl options5_vtbl = {
    options_QueryInterface,
    static_AddRef,
    static_Release,
    options_get_EnableTrackingPrevention,
    options_put_EnableTrackingPrevention,
};
static ICoreWebView2EnvironmentOptions5 options5 = {&options5_vtbl};

static ICoreWebView2EnvironmentOptions6Vtbl options6_vtbl = {
    options_QueryInterface,
    static_AddRef,
    static_Release,
    options_get_AreBrowserExtensionsEnabled,
    options_put_AreBrowserExtensionsEnabled,
};
static ICoreWebView2EnvironmentOptions6 options6 = {&options6_vtbl};

static ICoreWebView2EnvironmentOptions7Vtbl options7_vtbl = {
    options_QueryInterface,
    static_AddRef,
    static_Release,
    options_get_ChannelSearchKind,
    options_put_ChannelSearchKind,
    options_get_ReleaseChannels,
    options_put_ReleaseChannels,
};
static ICoreWebView2EnvironmentOptions7 options7 = {&options7_vtbl};

static ICoreWebView2EnvironmentOptions8Vtbl options8_vtbl = {
    options_QueryInterface,
    static_AddRef,
    static_Release,
    options_get_ScrollBarStyle,
    options_put_ScrollBarStyle,
};
static ICoreWebView2EnvironmentOptions8 options8 = {&options8_vtbl};

const IID webview2_CLSID = {0x26d34152, 0x879f, 0x4065, {0xbe, 0xa2, 0x3d, 0xaa, 0x2c, 0xfa, 0xdf, 0xb8}};

ICoreWebView2EnvironmentOptions *get_webview_environment_options() {
    return NULL;
    // if (CoInitializeEx(NULL, COINIT_APARTMENTTHREADED) != S_OK) {
    //     return NULL;
    // }
    // // ICoreWebView2CustomSchemeRegistration *registration1 = NULL;
    // // ICoreWebView2CustomSchemeRegistration *registration2 = NULL;
    // // CoCreateInstance(&webview2_CLSID,
    // //                  0,
    // //                  CLSCTX_INPROC_SERVER,
    // //                  &IID_ICoreWebView2CustomSchemeRegistration,
    // //                  (void *) &registration1);
    // //
    // // CoCreateInstance(&webview2_CLSID,
    // //                  0,
    // //                  CLSCTX_INPROC_SERVER,
    // //                  &IID_ICoreWebView2CustomSchemeRegistration,
    // //                  (void *) &registration2);
    // // if (registration1 == NULL) {
    // //     return NULL;
    // // }
    // // if (registration2 == NULL) {
    // //     return NULL;
    // // }
    //
    //
    // ICoreWebView2EnvironmentOptions *options = NULL;
    // HRESULT hr = CoCreateInstance(&webview2_CLSID,
    //                               0,
    //                               CLSCTX_INPROC_SERVER,
    //                               &IID_ICoreWebView2EnvironmentOptions,
    //                               (void *) &options);
    //
    // return options;
    //
    // if (options == NULL) {
    //     return NULL;
    // }
    //
    // ICoreWebView2EnvironmentOptions4 *options4 = NULL;
    // options->lpVtbl->QueryInterface(options, &IID_ICoreWebView2EnvironmentOptions4, &options4);
    // options4->lpVtbl->SetCustomSchemeRegistrations(options4, 2, registrations);
    // options4->lpVtbl->Release(options4);
    //
    // return options;

    // g_mutex_lock(&mutex);
    // options1_ptr = &options1;
    // options2_ptr = &options2;
    // options3_ptr = &options3;
    // options4_ptr = &options4;
    // options5_ptr = &options5;
    // options6_ptr = &options6;
    // options7_ptr = &options7;
    // options8_ptr = &options8;
    //
    // g_mutex_unlock(&mutex);
    // return &options1;
}

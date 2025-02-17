#ifdef _WIN32
#include "komelia_enumerate_devices.h"
#include "devices_common_jni.h"
#include <dxgi1_4.h>
#include "../win32_strings.h"

bool IsSoftwareAdapter(DXGI_ADAPTER_DESC1 desc) {
    // see here for documentation on filtering WARP adapter:
    // https://docs.microsoft.com/en-us/windows/desktop/direct3ddxgi/d3d10-graphics-programming-guide-dxgi#new-info-about-enumerating-adapters-for-windows-8
    auto isBasicRenderDriverVendorId = desc.VendorId == 0x1414;
    auto isBasicRenderDriverDeviceId = desc.DeviceId == 0x8c;
    auto isSoftwareAdapter = desc.Flags == DXGI_ADAPTER_FLAG_SOFTWARE;

    return isSoftwareAdapter || (isBasicRenderDriverVendorId && isBasicRenderDriverDeviceId);
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_OnnxRuntimeUpscaler_enumerateDevices(
        JNIEnv *env,
        jobject this
) {
    IDXGIFactory1 *pFactory = nullptr;
    IDXGIAdapter1 *pAdapter;
    HRESULT hr;

    hr = CreateDXGIFactory(&IID_IDXGIFactory, (void **) (&pFactory));
    if (hr != S_OK) {
        char message[64];
        snprintf(message, 64, "DXGI error: HRESULT 0x%x", hr);
        throw_jvm_exception(env, message);
        return nullptr;
    }

    jobject jvm_list = create_jvm_list(env);
    for (UINT i = 0; pFactory->lpVtbl->EnumAdapters1(pFactory, i, &pAdapter) != DXGI_ERROR_NOT_FOUND; ++i) {
        struct DeviceInfo info;
        DXGI_ADAPTER_DESC1 desc;
        pAdapter->lpVtbl->GetDesc1(pAdapter, &desc);

        if (IsSoftwareAdapter(desc)) {
            pAdapter->lpVtbl->Release(pAdapter);
            continue;
        }

        info.name = toUTF8(desc.Description, 0, nullptr);
        info.id = (int) i;
        info.memory = desc.DedicatedVideoMemory + desc.DedicatedSystemMemory + desc.SharedSystemMemory;
        add_to_jvm_list(env, jvm_list, info);

        pAdapter->lpVtbl->Release(pAdapter);
    }

    pFactory->lpVtbl->Release(pFactory);

    return jvm_list;
}
#endif

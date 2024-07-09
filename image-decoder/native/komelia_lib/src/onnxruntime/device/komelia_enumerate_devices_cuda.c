#include "komelia_enumerate_devices.h"
#include "devices_common_jni.h"
#include <cuda_runtime.h>


JNIEXPORT jobject JNICALL Java_io_github_snd_1r_OnnxRuntimeUpscaler_enumerateDevices(
        JNIEnv *env,
        jobject this
) {
    int nDevices;
    cudaError_t status = cudaGetDeviceCount(&nDevices);
    if (status != cudaSuccess) {
        throw_jvm_exception(env, cudaGetErrorName(status));
    }

    jobject jvm_list = create_jvm_list(env);
    for (int i = 0; i < nDevices; ++i) {
        struct cudaDeviceProp prop;
        cudaGetDeviceProperties(&prop, i);
        struct DeviceInfo info;
        info.name = prop.name;
        info.id = i;
        info.memory = prop.totalGlobalMem;
        add_to_jvm_list(env, jvm_list, info);
    }

    return jvm_list;
}

#include "devices_common_jni.h"
#include "komelia_enumerate_devices.h"
#include <hip/hip_runtime.h>

JNIEXPORT jobject JNICALL
Java_snd_komelia_image_OnnxRuntimeUpscaler_enumerateDevices(JNIEnv *env, jobject this) {
  int nDevices;
  hipError_t status = hipGetDeviceCount(&nDevices);
  if (status != hipSuccess) {
    throw_jvm_exception(env, hipGetErrorName(status));
    return NULL;
  }

  jobject jvm_list = create_jvm_list(env);
  for (int i = 0; i < nDevices; ++i) {
    hipDeviceProp_t prop;
    hipGetDeviceProperties(&prop, i);
    struct DeviceInfo info;
    info.name = prop.name;
    info.id = i;
    info.memory = prop.totalGlobalMem;
    add_to_jvm_list(env, jvm_list, info);
  }

  return jvm_list;
}
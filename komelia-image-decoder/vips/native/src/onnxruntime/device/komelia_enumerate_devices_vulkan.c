#include "komelia_enumerate_devices.h"
#include "devices_common_jni.h"
#include <vulkan/vulkan.h>

static inline const char *string_VkResult(VkResult input_value) {
    switch (input_value) {
        case VK_SUCCESS:
            return "VK_SUCCESS";
        case VK_NOT_READY:
            return "VK_NOT_READY";
        case VK_TIMEOUT:
            return "VK_TIMEOUT";
        case VK_EVENT_SET:
            return "VK_EVENT_SET";
        case VK_EVENT_RESET:
            return "VK_EVENT_RESET";
        case VK_INCOMPLETE:
            return "VK_INCOMPLETE";
        case VK_ERROR_OUT_OF_HOST_MEMORY:
            return "VK_ERROR_OUT_OF_HOST_MEMORY";
        case VK_ERROR_OUT_OF_DEVICE_MEMORY:
            return "VK_ERROR_OUT_OF_DEVICE_MEMORY";
        case VK_ERROR_INITIALIZATION_FAILED:
            return "VK_ERROR_INITIALIZATION_FAILED";
        case VK_ERROR_DEVICE_LOST:
            return "VK_ERROR_DEVICE_LOST";
        case VK_ERROR_MEMORY_MAP_FAILED:
            return "VK_ERROR_MEMORY_MAP_FAILED";
        case VK_ERROR_LAYER_NOT_PRESENT:
            return "VK_ERROR_LAYER_NOT_PRESENT";
        case VK_ERROR_EXTENSION_NOT_PRESENT:
            return "VK_ERROR_EXTENSION_NOT_PRESENT";
        case VK_ERROR_FEATURE_NOT_PRESENT:
            return "VK_ERROR_FEATURE_NOT_PRESENT";
        case VK_ERROR_INCOMPATIBLE_DRIVER:
            return "VK_ERROR_INCOMPATIBLE_DRIVER";
        case VK_ERROR_TOO_MANY_OBJECTS:
            return "VK_ERROR_TOO_MANY_OBJECTS";
        case VK_ERROR_FORMAT_NOT_SUPPORTED:
            return "VK_ERROR_FORMAT_NOT_SUPPORTED";
        case VK_ERROR_FRAGMENTED_POOL:
            return "VK_ERROR_FRAGMENTED_POOL";
        case VK_ERROR_UNKNOWN:
            return "VK_ERROR_UNKNOWN";
        case VK_ERROR_OUT_OF_POOL_MEMORY:
            return "VK_ERROR_OUT_OF_POOL_MEMORY";
        case VK_ERROR_INVALID_EXTERNAL_HANDLE:
            return "VK_ERROR_INVALID_EXTERNAL_HANDLE";
        case VK_ERROR_FRAGMENTATION:
            return "VK_ERROR_FRAGMENTATION";
        case VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS:
            return "VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS";
        case VK_PIPELINE_COMPILE_REQUIRED:
            return "VK_PIPELINE_COMPILE_REQUIRED";
        case VK_ERROR_SURFACE_LOST_KHR:
            return "VK_ERROR_SURFACE_LOST_KHR";
        case VK_ERROR_NATIVE_WINDOW_IN_USE_KHR:
            return "VK_ERROR_NATIVE_WINDOW_IN_USE_KHR";
        case VK_SUBOPTIMAL_KHR:
            return "VK_SUBOPTIMAL_KHR";
        case VK_ERROR_OUT_OF_DATE_KHR:
            return "VK_ERROR_OUT_OF_DATE_KHR";
        case VK_ERROR_INCOMPATIBLE_DISPLAY_KHR:
            return "VK_ERROR_INCOMPATIBLE_DISPLAY_KHR";
        case VK_ERROR_VALIDATION_FAILED_EXT:
            return "VK_ERROR_VALIDATION_FAILED_EXT";
        case VK_ERROR_INVALID_SHADER_NV:
            return "VK_ERROR_INVALID_SHADER_NV";
        case VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT:
            return "VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT";
        case VK_ERROR_NOT_PERMITTED_KHR:
            return "VK_ERROR_NOT_PERMITTED_KHR";
        case VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT:
            return "VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT";
        case VK_THREAD_IDLE_KHR:
            return "VK_THREAD_IDLE_KHR";
        case VK_THREAD_DONE_KHR:
            return "VK_THREAD_DONE_KHR";
        case VK_OPERATION_DEFERRED_KHR:
            return "VK_OPERATION_DEFERRED_KHR";
        case VK_OPERATION_NOT_DEFERRED_KHR:
            return "VK_OPERATION_NOT_DEFERRED_KHR";
#ifdef VK_ENABLE_BETA_EXTENSIONS
            case VK_ERROR_INVALID_VIDEO_STD_PARAMETERS_KHR:
            return "VK_ERROR_INVALID_VIDEO_STD_PARAMETERS_KHR";
#endif  // VK_ENABLE_BETA_EXTENSIONS
        case VK_ERROR_COMPRESSION_EXHAUSTED_EXT:
            return "VK_ERROR_COMPRESSION_EXHAUSTED_EXT";
        default:
            return "Unhandled VkResult";
    }
}

VkInstance instance = NULL;

JNIEXPORT jobject JNICALL Java_snd_komelia_image_OnnxRuntimeUpscaler_enumerateDevices(
        JNIEnv *env,
        jobject this
) {
    VkInstanceCreateInfo createInfo = {0};
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;

    if (instance == NULL) {
        VkResult result = vkCreateInstance(&createInfo, NULL, &instance);
        if (result != VK_SUCCESS) {
            throw_jvm_exception(env, string_VkResult(result));
            return NULL;
        }
    }

    uint32_t device_count;
    VkResult result;
    result = vkEnumeratePhysicalDevices(instance, &device_count, NULL);
    if (result != VK_SUCCESS) {
        throw_jvm_exception(env, string_VkResult(result));
        return NULL;
    }

    VkPhysicalDevice devices[device_count];
    if (device_count > 0) {
        result = vkEnumeratePhysicalDevices(instance, &device_count, devices);
        if (result != VK_SUCCESS) {
            throw_jvm_exception(env, string_VkResult(result));
            return NULL;
        }
    } else {
        throw_jvm_exception(env, "No Vulkan compatible devices found");
        return NULL;
    }

    jobject jvm_list = create_jvm_list(env);
    for (int i = 0; i < device_count; ++i) {
        VkPhysicalDeviceProperties device_properties = {0};
        VkPhysicalDeviceMemoryProperties mem_props = {0};
        vkGetPhysicalDeviceProperties(devices[i], &device_properties);
        vkGetPhysicalDeviceMemoryProperties(devices[i], &mem_props);

        VkDeviceSize max_heap_size = 0;
        for (int heap_index = 0; heap_index < mem_props.memoryHeapCount; ++heap_index) {
            VkMemoryHeap heap = mem_props.memoryHeaps[heap_index];
            if ((heap.flags & VK_MEMORY_HEAP_DEVICE_LOCAL_BIT) != 0) {
                max_heap_size = max_heap_size < heap.size ? heap.size : max_heap_size;
            }
        }

        struct DeviceInfo info;
        info.name = device_properties.deviceName;
        info.id = i;
        info.memory = max_heap_size;
        add_to_jvm_list(env, jvm_list, info);
    }

    return jvm_list;
}

#include "onnxruntime_conversions.h"
#include <math.h>
#include <time.h>

double millis() {
  struct timespec now;

#ifdef _WIN32
  clock_gettime(CLOCK_REALTIME, &now);
#else
  timespec_get(&now, TIME_UTC);
#endif
  return ((double)now.tv_sec) * 1000.0 + ((double)now.tv_nsec) / 1000000.0;
}

void hwc_to_chw(const uint8_t *input, size_t height, size_t width, size_t bands, float *output) {

  size_t stride_size = height * width;
#pragma omp parallel for shared(stride_size, bands, input, output) default(none) collapse(2)
  for (size_t i = 0; i != stride_size; ++i) {
    for (size_t c = 0; c != bands; ++c) {
      output[c * stride_size + i] = (float)input[i * bands + c] / 255.0f;
    }
  }
}

void hwc_to_chw_f16(const uint8_t *input, size_t height, size_t width, size_t bands,
                    _Float16 *output) {
  size_t stride_size = height * width;
#pragma omp parallel for shared(stride_size, bands, input, output) default(none) collapse(2)
  for (size_t i = 0; i != stride_size; ++i) {
    for (size_t c = 0; c != bands; ++c) {
      output[c * stride_size + i] = (_Float16)input[i * bands + c] / 255.0f16;
    }
  }
}

void chw_to_hwc(const float *input, size_t height, size_t width, size_t bands, uint8_t *output) {
  size_t stride_size = height * width;
  for (size_t c = 0; c != bands; ++c) {
    size_t t = c * stride_size;
#pragma omp parallel for shared(c, t, stride_size, bands, input, output) default(none)
    for (size_t i = 0; i != stride_size; ++i) {
      float f = input[t + i];

      if (f < 0.f) {
        f = 0.f;
      } else if (f > 1.f) {
        f = 1.f;
      }

      output[i * bands + c] = (uint8_t)nearbyintf(f * 255);
    }
  }
}

void chw_to_hwc_f16(const _Float16 *input, size_t height, size_t width, size_t bands,
                    uint8_t *output) {
  size_t stride_size = height * width;
  for (size_t c = 0; c != bands; ++c) {
    size_t t = c * stride_size;
#pragma omp parallel for shared(c, t, stride_size, bands, input, output) default(none)
    for (size_t i = 0; i != stride_size; ++i) {
      _Float16 f = input[t + i];

      if (f < 0.f16) {
        f = 0.f16;
      } else if (f > 1.f16) {
        f = 1.f16;
      }

      output[i * bands + c] = (uint8_t)nearbyintf((float)f * 255);
    }
  }
}
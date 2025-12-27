#ifndef KOMELIA_ORT_CONVERSIONS_H
#define KOMELIA_ORT_CONVERSIONS_H

#include <math.h>
#include <stddef.h>
#include <stdint.h>

// transform [c,h,w] pixel matrix to [h,w,c]
// channel(rgb) x [width, height]
static void komelia_chw_to_hwc(
    const float *input,
    size_t height,
    size_t width,
    size_t channels,
    uint8_t *output
) {
    size_t stride_size = height * width;
    for (size_t c = 0; c != channels; ++c) {
        size_t t = c * stride_size;
#pragma omp parallel for shared(c, t, stride_size, channels, input, output) default(none)
        for (size_t i = 0; i != stride_size; ++i) {
            float f = input[t + i];

            if (f < 0.f) {
                f = 0.f;
            } else if (f > 1.f) {
                f = 1.f;
            }

            output[i * channels + c] = (uint8_t)nearbyintf(f * 255);
        }
    }
}

static void komelia_chw_to_hwc_f16(
    const _Float16 *input,
    size_t height,
    size_t width,
    size_t channels,
    uint8_t *output
) {
    size_t stride_size = height * width;
    for (size_t c = 0; c != channels; ++c) {
        size_t t = c * stride_size;
#pragma omp parallel for shared(c, t, stride_size, channels, input, output) default(none)
        for (size_t i = 0; i != stride_size; ++i) {
            _Float16 f = input[t + i];

            if (f < 0.f16) {
                f = 0.f16;
            } else if (f > 1.f16) {
                f = 1.f16;
            }

            output[i * channels + c] = (uint8_t)nearbyintf((float)f * 255);
        }
    }
}

static void komelia_transpose2d(
    const float *src,
    float *dst,
    const int N,
    const int M
) {
#pragma omp parallel for shared(src, dst, N, M) default(none)
    for (int n = 0; n < N * M; n++) {
        int i = n / N;
        int j = n % N;
        dst[n] = src[M * j + i];
    }
}

#endif // KOMELIA_ORT_CONVERSIONS_H
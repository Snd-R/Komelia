#ifndef KOMELIA_ORT_CONVERSIONS_H
#define KOMELIA_ORT_CONVERSIONS_H

#include <stddef.h>
#include <stdint.h>

void hwc_to_chw(const uint8_t *input,
                size_t height, size_t width, size_t bands,
                float *output
);

void hwc_to_chw_f16(const uint8_t *input,
                    size_t height, size_t width, size_t bands,
                    _Float16 *output
);

void chw_to_hwc(const float *input,
                size_t height, size_t width, size_t bands,
                uint8_t *output
);

void chw_to_hwc_f16(const _Float16 *input,
                    size_t height, size_t width, size_t bands,
                    uint8_t *output
);

#endif //KOMELIA_ORT_CONVERSIONS_H

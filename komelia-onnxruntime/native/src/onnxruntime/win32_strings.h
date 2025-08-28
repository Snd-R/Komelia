#ifndef KOMELIA_WIN32_STRINGS_H
#define KOMELIA_WIN32_STRINGS_H

#ifdef _WIN32
#include <windows.h>

static wchar_t *fromUTF8(
    const char *src,
    size_t src_length,
    size_t *out_length
) {
    if (!src) {
        return nullptr;
    }

    if (src_length == 0) {
        src_length = strlen(src);
    }
    int length = MultiByteToWideChar(CP_UTF8, 0, src, (int)src_length, 0, 0);
    wchar_t *output_buffer = (wchar_t *)malloc((length + 1) * sizeof(wchar_t));
    if (output_buffer) {
        MultiByteToWideChar(CP_UTF8, 0, src, (int)src_length, output_buffer, length);
        output_buffer[length] = L'\0';
    }
    if (out_length) {
        *out_length = length;
    }
    return output_buffer;
}

static char *toUTF8(
    const wchar_t *src,
    size_t src_length,
    size_t *out_length
) {
    if (!src) {
        return nullptr;
    }

    if (src_length == 0) {
        src_length = wcslen(src);
    }
    int length = WideCharToMultiByte(CP_UTF8, 0, src, (int)src_length, 0, 0, nullptr, nullptr);
    char *output_buffer = (char *)malloc((length + 1) * sizeof(char));
    if (output_buffer) {
        WideCharToMultiByte(
            CP_UTF8, 0, src, (int)src_length, output_buffer, length, nullptr, nullptr
        );
        output_buffer[length] = '\0';
    }
    if (out_length) {
        *out_length = length;
    }
    return output_buffer;
}

#endif

#endif // KOMELIA_WIN32_STRINGS_H
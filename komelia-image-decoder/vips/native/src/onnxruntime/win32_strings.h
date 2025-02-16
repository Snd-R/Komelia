#ifndef KOMELIA_WIN32_STRINGS_H
#define KOMELIA_WIN32_STRINGS_H

#ifdef _WIN32
#include <wchar.h>

wchar_t *fromUTF8(const char *src, size_t src_length, size_t *out_length);

char *toUTF8(const wchar_t *src, size_t src_length, size_t *out_length);

#endif

#endif // KOMELIA_WIN32_STRINGS_H
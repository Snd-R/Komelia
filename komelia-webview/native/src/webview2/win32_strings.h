#ifndef KOMELIA_WIN32_STRINGS_H
#define KOMELIA_WIN32_STRINGS_H

#ifdef _WIN32
#include <wchar.h>


wchar_t *fromUTF8(
        const char *src,
        size_t src_length,  /* = 0 */
        size_t *out_length  /* = NULL */
);

char *toUTF8(
        const wchar_t *src,
        size_t src_length,  /* = 0 */
        size_t *out_length  /* = NULL */
);

#endif

#endif //KOMELIA_WIN32_STRINGS_H

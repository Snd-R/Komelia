#ifndef ERROR_CODES_H
#define ERROR_CODES_H
#include <glib.h>

GQuark komelia_ort_error_quark(void);
#define KOMELIA_ORT_ERROR komelia_ort_error_quark()

typedef enum {
    KOMELIA_ORT_ERROR_UNSUPPORTED_API_VERSION = -2,
    KOMELIA_ORT_ERROR_EXECUTION_PROVIDER_INIT = -3,
    KOMELIA_ORT_ERROR_SESSION_INIT = -3,
    KOMELIA_ORT_ERROR_INFERENCE = -4,
    KOMELIA_ORT_ERROR_VIPS = -4,
    KOMELIA_ORT_ERROR_UNKNOWN = -5,
} KomeliaOrtError;

#endif // ERROR_CODES_H

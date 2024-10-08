cmake_minimum_required(VERSION 3.25)


find_program(Make_EXECUTABLE make)
if(NOT Make_EXECUTABLE)
    message(FATAL_ERROR "Make is required")
endif()

find_program(Meson_EXECUTABLE meson)
if(NOT Meson_EXECUTABLE)
    message(FATAL_ERROR "Meson is required")
endif()

find_program(Ninja_EXECUTABLE ninja)
if(NOT Ninja_EXECUTABLE)
    message(FATAL_ERROR "Ninja is required")
endif()

project(komelia_superbuild C CXX)

set(THIRD_PARTY_LIB_PATH ${CMAKE_BINARY_DIR}/fakeroot)
set(CMAKE_FIND_ROOT_PATH ${THIRD_PARTY_LIB_PATH}/lib;${CMAKE_FIND_ROOT_PATH})
set_directory_properties(PROPERTIES EP_PREFIX ${THIRD_PARTY_LIB_PATH})

include("cmake/zlib.cmake")
include("cmake/brotli.cmake")
include("cmake/ffi.cmake")
include("cmake/iconv.cmake")
include("cmake/expat.cmake")
include("cmake/glib.cmake")
include("cmake/highway.cmake")

include("cmake/mozjpeg.cmake")
include("cmake/jxl.cmake")
include("cmake/spng.cmake")
include("cmake/webp.cmake")
include("cmake/dav1d.cmake")
include("cmake/de265.cmake")
include("cmake/heif.cmake")
include("cmake/tiff.cmake")

include("cmake/vips.cmake")
#include("cmake/onnxruntime.cmake")
include("cmake/komelia.cmake")

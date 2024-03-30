cmake_minimum_required(VERSION 3.18)

option(USE_SUPERBUILD "superbuild" ON)

if (USE_SUPERBUILD)
    project(superbuild C CXX)

    include(CTest)
    include(CMakeFindDependencyMacro)

    set(THIRD_PARTY_LIB_PATH ${CMAKE_BINARY_DIR}/fakeroot)
    set_directory_properties(PROPERTIES EP_PREFIX ${THIRD_PARTY_LIB_PATH})
    set(CMAKE_FIND_ROOT_PATH ${THIRD_PARTY_LIB_PATH})

    set(DEPENDENCIES)
    set(EXTRA_CMAKE_ARGS)

    find_program(Make_EXECUTABLE make)
    if (NOT Make_EXECUTABLE)
        message(FATAL_ERROR "Make is required")
    endif ()

    find_program(Meson_EXECUTABLE meson)
    if (NOT Meson_EXECUTABLE)
        message(FATAL_ERROR "Meson is required")
    endif ()

    find_program(Ninja_EXECUTABLE ninja)
    if (NOT Ninja_EXECUTABLE)
        message(FATAL_ERROR "Ninja is required")
    endif ()

    find_program(CTest_EXECUTABLE ctest)
    if (NOT CTest_EXECUTABLE)
        message(FATAL_ERROR "CTest is required")
    endif ()

    if (${CMAKE_SYSTEM_NAME} STREQUAL "Android")
        find_library(ICONV_LIB NAMES iconv PATHS ${THIRD_PARTY_LIB_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
        if (NOT ICONV_LIB)
            include("cmake/iconv.cmake")
        endif ()

        find_library(PCRE2_LIB NAMES pcre2-8 PATHS ${THIRD_PARTY_LIB_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
        if (NOT PCRE2_LIB)
            include("cmake/pcre2.cmake")
        endif ()
    endif ()

    find_library(FFI_LIB NAMES ffi PATHS ${THIRD_PARTY_LIB_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
    if (NOT FFI_LIB)
        include("cmake/ffi.cmake")
    endif ()

    find_library(ZLIB_LIB NAMES zlib PATHS ${THIRD_PARTY_LIB_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
    if (NOT ZLIB_LIB)
        include("cmake/zlib.cmake")
    endif ()

    find_library(GLIB_LIB NAMES glib-2.0 PATHS ${THIRD_PARTY_LIB_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
    if (NOT GLIB_LIB)
        include("cmake/glib.cmake")
    endif ()

    find_library(EXPAT_LIB NAMES expat PATHS ${THIRD_PARTY_LIB_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
    if (NOT EXPAT_LIB)
        include("cmake/expat.cmake")
    endif ()

    find_package(mozjpeg PATHS ${THIRD_PARTY_LIB_PATH} NO_DEFAULT_PATH)
    if (NOT mozjpeg_FOUND)
        include("cmake/mozjpeg.cmake")
    endif ()

    find_package(WebP PATHS ${THIRD_PARTY_LIB_PATH} NO_DEFAULT_PATH)
    if (NOT WebP_FOUND)
        include("cmake/webp.cmake")
    endif ()

    find_package(SPNG PATHS ${THIRD_PARTY_LIB_PATH} NO_DEFAULT_PATH)
    if (NOT SPNG_FOUND)
        include("cmake/spng.cmake")
    endif ()

    find_package(Tiff PATHS ${THIRD_PARTY_LIB_PATH} NO_DEFAULT_PATH)
    if(NOT Tiff_FOUND)
        include("cmake/tiff.cmake")
    endif()

    find_package(FFTW3 PATHS ${THIRD_PARTY_LIB_PATH} NO_DEFAULT_PATH)
    if (NOT FFTW3_FOUND)
        include("cmake/fftw.cmake")
    endif ()

    find_library(dav1d_LIB NAMES dav1d PATHS ${THIRD_PARTY_LIB_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
    if (NOT dav1d_LIB)
        include("cmake/dav1d.cmake")
    endif ()

    #find_library(de265_LIB NAMES de265 PATHS ${THIRD_PARTY_LIB_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
    #if (NOT de265_LIB)
    #    include("cmake/de265.cmake")
    #endif ()

    find_library(heif_LIB NAMES heif PATHS ${THIRD_PARTY_LIB_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
    if (NOT heif_LIB)
        include("cmake/heif.cmake")
    endif ()

    find_library(highway_LIB NAMES heif PATHS ${THIRD_PARTY_LIB_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
    if (NOT highway_LIB)
        include("cmake/highway.cmake")
    endif ()

    find_library(VIPS_LIB NAMES vips PATHS ${THIRD_PARTY_LIB_PATH} PATH_SUFFIXES lib NO_DEFAULT_PATH)
    if (NOT VIPS_LIB)
        include("cmake/vips.cmake")
    endif ()
    include("cmake/komelia.cmake")
    return()
else ()
    project(komelia C)
endif ()

add_subdirectory(src)
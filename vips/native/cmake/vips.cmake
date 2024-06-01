include(ExternalProject)

find_library(VIPS_LIB NAMES vips PATHS ${CMAKE_BINARY_DIR}/fakeroot PATH_SUFFIXES lib NO_DEFAULT_PATH)
if(VIPS_LIB)
    return()
endif()

include("cmake/iconv.cmake")
include("cmake/zlib.cmake")
include("cmake/ffi.cmake")
include("cmake/glib.cmake")
include("cmake/expat.cmake")
include("cmake/fftw.cmake")
include("cmake/highway.cmake")

include("cmake/mozjpeg.cmake")
include("cmake/jxl.cmake")
include("cmake/spng.cmake")
include("cmake/webp.cmake")
include("cmake/dav1d.cmake")
include("cmake/de265.cmake")
include("cmake/heif.cmake")
include("cmake/tiff.cmake")

ExternalProject_Add(ep_vips
        GIT_REPOSITORY https://github.com/libvips/libvips.git
        GIT_TAG v8.15.2
        DEPENDS ep_expat ep_fftw ep_glib ep_heif ep_highway ep_jxl ep_spng ep_webp ep_tiff
        CONFIGURE_COMMAND
            ${Meson_EXECUTABLE} setup ${MESON_CROSS_FILE_ARG} --default-library shared --prefix=<INSTALL_DIR> --libdir=lib --buildtype=release
            -Ddeprecated=false
            -Dexamples=false
            -Dcplusplus=false
            -Ddoxygen=false
            -Dgtk_doc=false
            -Dmodules=disabled
            -Dintrospection=disabled
            -Dvapi=false
            -Dcfitsio=disabled
            -Dcgif=enabled
            -Dexif=disabled
            -Dfftw=enabled
            -Dfontconfig=disabled
            -Darchive=disabled
            -Dheif=enabled
            -Dheif-module=disabled
            -Dimagequant=disabled
            -Djpeg=enabled
            -Djpeg-xl=enabled
            -Djpeg-xl-module=disabled
            -Dlcms=disabled
            -Dmagick=disabled
            -Dmatio=disabled
            -Dnifti=disabled
            -Dopenexr=disabled
            -Dopenjpeg=disabled
            -Dopenslide=disabled
            -Dhighway=enabled
            -Dorc=disabled
            -Dpangocairo=disabled
            -Dpdfium=disabled
            -Dpng=disabled
            -Dpoppler=disabled
            -Dquantizr=disabled
            -Drsvg=disabled
            -Dspng=enabled
            -Dtiff=enabled
            -Dwebp=enabled
            <BINARY_DIR> <SOURCE_DIR>
        BUILD_COMMAND
            ${FIX_ANDROID_BUILD} ${Ninja_EXECUTABLE} -C <BINARY_DIR>
        INSTALL_COMMAND
            ${Ninja_EXECUTABLE} -C <BINARY_DIR> install
)
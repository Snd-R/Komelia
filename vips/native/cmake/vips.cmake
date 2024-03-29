include(ExternalProject)

list(APPEND DEPENDENCIES ep_vips)

if ($ENV{TARGET} MATCHES ".*android.*")
    set(FIX_ANDROID_BUILD sed -i "s! /usr/lib/libz.so!!g" <BINARY_DIR>/build.ninja &&)
endif ()

ExternalProject_Add(ep_vips
        GIT_REPOSITORY https://github.com/libvips/libvips.git
        GIT_TAG v8.15.2
        CONFIGURE_COMMAND
        PKG_CONFIG_PATH=${THIRD_PARTY_LIB_PATH}/lib/pkgconfig PKG_CONFIG_PATH_CUSTOM=${THIRD_PARTY_LIB_PATH}/lib/pkgconfig LD_LIBRARY_PATH=${THIRD_PARTY_LIB_PATH}/lib:$ENV{LD_LIBRARY_PATH}
        ${Meson_EXECUTABLE} setup ${MESON_CROSS_FILE_ARG} --default-library shared --prefix=<INSTALL_DIR> --libdir=lib --buildtype=release
        -Dheif=enabled
        -Dhighway=enabled
        -Djpeg=enabled
        -Dwebp=enabled
        -Dspng=enabled
        -Dtiff=enabled
        -Dexamples=false
        -Dcplusplus=false
        -Dintrospection=disabled
        -Dmagick=disabled
        -Dpangocairo=disabled
        -Dpoppler=disabled
        -Dopenexr=disabled
        -Djpeg-xl=disabled
        -Dlcms=disabled
        -Dexif=disabled
        -Dheif-module=disabled
        -Dmodules=disabled
        -Dopenjpeg=disabled
        -Dorc=disabled
        -Dpng=disabled
        -Drsvg=disabled
        -Dpdfium=disabled
        -Dopenslide=disabled
        -Dnifti=disabled
        -Dmatio=disabled
        <BINARY_DIR> <SOURCE_DIR>
        BUILD_COMMAND
        ${FIX_ANDROID_BUILD} ${Ninja_EXECUTABLE} -C <BINARY_DIR>
        INSTALL_COMMAND
        ${Ninja_EXECUTABLE} -C <BINARY_DIR> install
        DEPENDS
        ep_expat
        ep_fftw
        ep_glib
        ep_heif
        ep_highway
        ep_mozjpeg
        ep_spng
        ep_webp
        ep_tiff
)
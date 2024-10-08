include(ExternalProject)

if (MESON_CROSS_FILE)
    set(MESON_CROSS_FILE_ARG --cross-file=${MESON_CROSS_FILE})
endif()

ExternalProject_Add(ep_vips
        GIT_REPOSITORY https://github.com/libvips/libvips.git
        #GIT_TAG v8.15.3
        GIT_TAG 50caa1922fa88e02c8cbfb3192fe8282d7da6eee
        DEPENDS ep_expat ep_glib ep_heif ep_highway ep_jxl ep_spng ep_webp ep_tiff ep_mozjpeg
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
            -Dfftw=disabled
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
            ${Ninja_EXECUTABLE} -C <BINARY_DIR>
        INSTALL_COMMAND
            ${Ninja_EXECUTABLE} -C <BINARY_DIR> install
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)
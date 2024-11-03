include(ExternalProject)

ExternalProject_Add(ep_vips
        GIT_REPOSITORY https://github.com/libvips/libvips.git
        GIT_TAG v8.16.0
        DEPENDS ep_expat ep_glib ep_heif ep_highway ep_jxl ep_spng ep_webp ep_tiff ep_mozjpeg
        CONFIGURE_COMMAND
            ${Meson_EXECUTABLE} setup ${EP_MESON_ARGS}
            -Dhighway=enabled
            -Djpeg-xl=enabled
            -Djpeg=enabled
            -Dcgif=enabled
            -Dheif=enabled
            -Dspng=enabled
            -Dtiff=enabled
            -Dwebp=enabled
            -Ddeprecated=false
            -Dexamples=false
            -Dcplusplus=false
            -Ddoxygen=false
            -Dgtk_doc=false
            -Dmodules=disabled
            -Dintrospection=disabled
            -Dvapi=false
            -Dcfitsio=disabled
            -Dexif=disabled
            -Dfftw=disabled
            -Dfontconfig=disabled
            -Darchive=disabled
            -Dheif-module=disabled
            -Dimagequant=disabled
            -Djpeg-xl-module=disabled
            -Dlcms=disabled
            -Dmagick=disabled
            -Dmatio=disabled
            -Dnifti=disabled
            -Dopenexr=disabled
            -Dopenjpeg=disabled
            -Dopenslide=disabled
            -Dorc=disabled
            -Dpangocairo=disabled
            -Dpdfium=disabled
            -Dpng=disabled
            -Dpoppler=disabled
            -Dquantizr=disabled
            -Drsvg=disabled
            <BINARY_DIR> <SOURCE_DIR>
        BUILD_COMMAND ${Ninja_EXECUTABLE} -C <BINARY_DIR>
        INSTALL_COMMAND ${Ninja_EXECUTABLE} -C <BINARY_DIR> install
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)
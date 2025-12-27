include(ExternalProject)

ExternalProject_Add(ep_vips
        GIT_REPOSITORY https://github.com/libvips/libvips.git
        GIT_TAG v8.18.0
        GIT_SHALLOW 1
        GIT_PROGRESS 1
        DEPENDS ep_expat ep_glib ep_heif ep_highway ep_jxl ep_spng ep_webp ep_tiff ep_jpeg-turbo ep_lcms2 ep_exif
        UPDATE_DISCONNECTED True
        PATCH_COMMAND git apply ${CMAKE_CURRENT_LIST_DIR}/patches/vips_thumbnail_resampling_kernel.patch
        CONFIGURE_COMMAND
            ${Meson_EXECUTABLE} setup ${EP_MESON_ARGS}
            -Dexif=enabled
            -Dhighway=enabled
            -Djpeg-xl=enabled
            -Djpeg=enabled
            -Dcgif=enabled
            -Dheif=enabled
            -Dlcms=enabled
            -Dpng=enabled
            -Dtiff=enabled
            -Dwebp=enabled
            -Ddeprecated=false
            -Dexamples=false
            -Dcplusplus=false
            -Dmodules=disabled
            -Dintrospection=disabled
            -Dvapi=false
            -Dcfitsio=disabled
            -Dfftw=disabled
            -Dfontconfig=disabled
            -Darchive=disabled
            -Dheif-module=disabled
            -Dimagequant=disabled
            -Djpeg-xl-module=disabled
            -Dmagick=disabled
            -Dmatio=disabled
            -Dnifti=disabled
            -Dopenexr=disabled
            -Dopenjpeg=disabled
            -Dopenslide=disabled
            -Dorc=disabled
            -Dpangocairo=disabled
            -Dpdfium=disabled
            -Dpoppler=disabled
            -Dquantizr=disabled
            -Dspng=disabled
            -Drsvg=disabled
            <BINARY_DIR> <SOURCE_DIR>
        BUILD_COMMAND ${Ninja_EXECUTABLE} -C <BINARY_DIR>
        INSTALL_COMMAND ${Ninja_EXECUTABLE} -C <BINARY_DIR> install
        USES_TERMINAL_DOWNLOAD true
        USES_TERMINAL_BUILD true
)

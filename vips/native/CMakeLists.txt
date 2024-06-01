cmake_minimum_required(VERSION 3.18)

option(USE_SUPERBUILD "superbuild" ON)

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

if (USE_SUPERBUILD)
    project(superbuild C CXX)

    set(THIRD_PARTY_LIB_PATH ${CMAKE_BINARY_DIR}/fakeroot)
    set(CMAKE_FIND_ROOT_PATH ${THIRD_PARTY_LIB_PATH}/lib;${CMAKE_FIND_ROOT_PATH})
    set_directory_properties(PROPERTIES EP_PREFIX ${THIRD_PARTY_LIB_PATH})
    include("cmake/vips.cmake")
    include("cmake/komelia.cmake")
    return()
else ()
    project(komelia C)
endif ()

add_subdirectory(src)
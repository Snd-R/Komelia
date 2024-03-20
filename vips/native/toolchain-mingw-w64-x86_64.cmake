set(BUILD_TARGET w64)

# the name of the target operating system
SET(CMAKE_SYSTEM_NAME Windows)
set(CMAKE_SYSTEM_PROCESSOR AMD64)

# which compilers to use for C and C++
SET(CMAKE_C_COMPILER x86_64-w64-mingw32-gcc)
SET(CMAKE_CXX_COMPILER x86_64-w64-mingw32-g++)
SET(CMAKE_RC_COMPILER x86_64-w64-mingw32-windres)
SET(CMAKE_AR x86_64-w64-mingw32-ar)
SET(CMAKE_LINKER x86_64-w64-mingw32-ld)
SET(CMAKE_RANLIB x86_64-w64-mingw32-ranlib)

# target environment on the build host system
set(CMAKE_FIND_ROOT_PATH /usr/x86_64-w64-mingw32;${CMAKE_INSTALL_PREFIX})

# modify default behavior of FIND_XXX() commands
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

set(MESON_CROSS_FILE_ARG --cross-file ${CMAKE_CURRENT_SOURCE_DIR}/cross_file.txt)
set(HOST_FLAG --host=x86_64-w64-mingw32)
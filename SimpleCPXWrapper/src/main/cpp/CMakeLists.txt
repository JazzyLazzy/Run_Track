
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("simplecpxwrapper")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        simplecpxwrapper

        # Sets the library as a shared library.
        SHARED


        include/SimpleCPXParser.c
        # Provides a relative path to your source file(s).
        simplecpxwrapper.cpp)

#target_include_directories(simplecpxwrapper PRIVATE src/main/cpp/include)


add_library(expat SHARED IMPORTED)

set_target_properties(
        expat
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/arm64-v8a/libexpat.so)

#add_library(SimpleCPXParser SHARED IMPORTED GLOBAL)

#set_target_properties(SimpleCPXParser PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/arm64-v8a/libSimpleCPXParser.so)

add_library(t SHARED IMPORTED)

set_target_properties( t PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/arm64-v8a/libt.so)

#include(${NDK_PATH}/platforms/android-${CMAKE_ANDROID_ABI}/usr/include/expat.h)
#link (${PROJECT_NAME} ${NDK_PATH}/platforms/android-${CMAKE_ANDROID_ABI}/lib/libext.a)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.


find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(simplecpxwrapper
        #SimpleCPXParser
        #t
        android
        expat
#        ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libexpat.a
 #       ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libSimpleCPXParser.a
        ${log-lib})

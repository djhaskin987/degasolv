# Taken from http://stackoverflow.com/a/17511705/850326
cmake_minimum_required(VERSION 3.5)

project (shiny
    VERSION 1.3.0
    LANGUAGES C)

set(CMAKE_BUILD_TYPE Release)


include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/external/include)

find_library(
    LEGACY_LIB legacy
    PATHS ${CMAKE_CURRENT_SOURCE_DIR}/external/lib NO_DEFAULT_PATH)

add_library(shiny
    SHARED src/shiny.c)

target_link_libraries(shiny ${LEGACY_LIB})

# Taken from http://stackoverflow.com/a/17511705/850326
cmake_minimum_required(VERSION 3.5)

project (legacy)
set(CMAKE_BUILD_TYPE Release)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)
add_library(legacy SHARED src/legacy.cpp)

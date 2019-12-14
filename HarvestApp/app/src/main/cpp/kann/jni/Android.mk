LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := kann
LOCAL_CFLAGS   := -I. -fPIC
LOCAL_CPPFLAGS   := -I.
LOCAL_SRC_FILES = ../kann.c ../kautodiff.c ../main.cpp

include $(BUILD_SHARED_LIBRARY)

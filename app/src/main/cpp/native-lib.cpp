#include <string>
#include <vector>
#include <unistd.h>
#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#define  LOG_TAG    "usb-display"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define QOI_NO_STDIO
#define QOI_IMPLEMENTATION
#include "qoi.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_github_fflexo_usb_1display_DisplayActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_github_fflexo_usb_1display_DisplayView_renderDisplay(JNIEnv * env, jclass clazz, jobject bitmap, jint fd, jlong size, jlong offset) {
    AndroidBitmapInfo  info;
    void *pixels;
    int ret;

    std::vector<char> compressed(size);

    lseek(fd, offset, SEEK_SET);
    const ssize_t got = read(fd, &compressed[0], size);
    LOGI("Read %zd bytes", got);
    //close(fd);

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed! error=%d", ret);
        return;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888!");
        return;
    }
    LOGI("%u x %u bitmap", info.width, info.height);

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed! error=%d", ret);
        return;
    }

    qoi_desc desc;
    void *decompressed = qoi_decode(&compressed[0], compressed.size(), &desc, 4);
    // TODO: info and desc should match perfectly or else soemthing went rather wrong and will only get worse!
    if(decompressed) {
        LOGI("decompressed %u x %u ok!", desc.width, desc.height);
        memcpy(pixels, decompressed, info.width*info.stride);
        free(decompressed);
    }
    else {
        LOGE("Decompress failed!");
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}

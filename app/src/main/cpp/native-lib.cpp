#include <string>
#include <vector>
#include <functional>
#include <utility>
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

extern "C" JNIEXPORT void JNICALL
Java_com_github_fflexo_usb_1display_DisplayView_renderDisplay(JNIEnv * env, jclass clazz, jobject bitmap, jbyteArray bytes) {
    AndroidBitmapInfo  info;
    int ret;

    // Careful - this is non-const pointer, but we've relaxed and promised not to change it!
    std::unique_ptr<jbyte, std::function<void (jbyte*)>> raw {
        env->GetByteArrayElements(bytes, NULL),
        [&](jbyte *elems){ env->ReleaseByteArrayElements(bytes, elems, JNI_ABORT);}
    };

    const jsize size = env->GetArrayLength(bytes);

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed! error=%d", ret);
        return;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888!");
        return;
    }
    LOGI("%u x %u bitmap", info.width, info.height);

    void *pixels = NULL;
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed! error=%d", ret);
        return;
    }

    qoi_desc desc;
    void *decompressed = qoi_decode(raw.get(), size, &desc, 4);
    // TODO: info and desc should match perfectly or else something went rather wrong and will only get worse!
    if(decompressed) {
        LOGI("decompressed %u x %u ok!", desc.width, desc.height);
        memcpy(pixels, decompressed, info.width*info.height*4);
        free(decompressed);
    }
    else {
        LOGE("Decompress failed!");
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}

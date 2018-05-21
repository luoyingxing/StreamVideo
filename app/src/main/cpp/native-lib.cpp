#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_conwin_video_jni_Test_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Bitmap";
    return env->NewStringUTF(hello.c_str());
}
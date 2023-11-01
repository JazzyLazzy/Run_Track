#include <jni.h>
#include <string>
#include "include/expat.h"
#include "include/SimpleCPXParser.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_lazarus_simplecpxwrapper_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    //GPX *gpx = static_cast<GPX *>(malloc(sizeof(GPX)));
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
        Java_com_lazarus_simplecpxwrapper_NativeLib_wtb(JNIEnv* env,
jobject thiz, jstring s){
    char *cfile = (char*) env->GetStringUTFChars(s, 0);

    //GPX *gpx = parse_GPX(cfile);
    return;
}

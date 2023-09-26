// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("run_track1");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("run_track1")
//      }
//    }

#include <jni.h>
#include <iostream>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_lazarus_run_1track1_NativeJNI_MonJNI_helloWorld(JNIEnv *env, jobject thiz) {
    // TODO: implement helloWorld()
    std::cout << "Hello, CPP" << std::endl;
    std::string message = "Hello JNI!";
    jstring javaString = env->NewStringUTF(message.c_str());

    // Return the Java string
    return javaString;
}

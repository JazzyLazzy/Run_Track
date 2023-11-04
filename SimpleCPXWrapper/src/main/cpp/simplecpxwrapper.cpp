#include <jni.h>
#include <string>
#include <android/log.h>
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

    GPX *gpx = parse_GPX(cfile);
    return;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_lazarus_simplecpxwrapper_NativeLib_parseGPX(JNIEnv *env, jobject thiz, jstring file) {
    // TODO: implement parse_GPX()
    char *cfile = (char*) env->GetStringUTFChars(file, 0);
    GPX *gpx = parse_GPX(cfile);

    jclass myCStructClass = env->FindClass("com/lazarus/simplecpxwrapper/CPXGeoPoint");
    jmethodID constructor = env->GetMethodID(myCStructClass, "<init>", "()V");
    jclass jLListClazz = env->FindClass("java/util/LinkedList");
    jmethodID lListConstructor = env->GetMethodID(jLListClazz, "<init>", "()V");
    jobject jLList = env->NewObject(jLListClazz, lListConstructor);

    Waypoint *wpt = gpx->waypoints;
    for (int i = 0; i < gpx->size; i++){
        wpt = wpt->next;
    }

    Location *loc = gpx->tracks->track_segs->locations;
    jclass jGeoPointClazz = env->FindClass("org/osmdroid/util/GeoPoint");
    jmethodID jGPConstructor = env->GetMethodID(jGeoPointClazz, "<init>", "(DD)V");
    while (loc->next){
        jobject jGeoPoint = env->NewObject(jGeoPointClazz, jGPConstructor, loc->latitude, loc->longitude);
        env->CallBooleanMethod(jLList, env->GetMethodID(env->FindClass("java/util/LinkedList"), "add", "(Ljava/lang/Object;)Z"), jGeoPoint);
        loc = loc->next;
    }

    jfieldID fieldId = env->GetFieldID(myCStructClass, "geoPointList", "Ljava/util/LinkedList;");
    jobject javaObject = env->NewObject(myCStructClass, constructor);
    env->SetObjectField(javaObject, fieldId, jLList);
    free_gpx(gpx);
    return javaObject;
}
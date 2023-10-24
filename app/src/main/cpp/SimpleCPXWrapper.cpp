//
// Created by lazarus on 19/10/2023.
//

#include "SimpleCPXWrapper.h"
#include "include/SimpleCPXParser.h"
#include "include/Track.h"
#include <jni.h>
#include <android/log.h>

/*extern "C"
JNIEXPORT jobject JNICALL
Java_com_lazarus_run_1track1_SimpleJPX_SimpleJPX_parseGPX(JNIEnv *env, jclass clazz,jstring file) {
    __android_log_write(ANDROID_LOG_ERROR, "Tag", "Error here");

    // TODO: implement parse_GPX()
    char *cfile = (char*) env->GetStringUTFChars(file, 0);
    GPX *gpx = parse_GPX(cfile);

    jclass myCStructClass = env->FindClass("com/lazarus/run_track1/CPXGeoPoint");
    jmethodID constructor = env->GetMethodID(myCStructClass, "<init>", "()V");
    jclass jLListClazz = env->FindClass("java/util/LinkedList");
    jmethodID lListConstructor = env->GetMethodID(jLListClazz, "<init>", "()V");
    jobject jLList = env->NewObject(jLListClazz, lListConstructor);

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

    return javaObject;
}*/

extern "C"
JNIEXPORT void JNICALL
Java_com_lazarus_run_1track1_SimpleJPX_SimpleJPX_wtb(JNIEnv *env, jobject thiz, jstring s) {
    // TODO: implement wtb()
    char *cfile = (char*) env->GetStringUTFChars(s, 0);
    GPX *gpx = parse_GPX(cfile);
    return;
}
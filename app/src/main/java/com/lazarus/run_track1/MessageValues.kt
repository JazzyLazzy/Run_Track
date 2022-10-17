package com.lazarus.run_track1

enum class MessageValues(val value:Int) {
    MSG_START_TRACKING(1),
    MSG_STOP_TRACKING(0),
    MSG_GET_TRKPT(2),
    MSG_GET_GPX(3),
    MSG_REGISTER_CLIENT(4),
    MSG_UNREGISTER_CLIENT(5)
}
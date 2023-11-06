package com.lazarus.simplecpxwrapper

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*


open class GPXParserLocation{

    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var elevation: Double = 0.0
    lateinit var time: LocalDateTime

    constructor(latitude: Double, longitude: Double, elevation:Double){
        this.latitude = latitude
        this.longitude = longitude
        this.elevation = elevation
        this.time = LocalDateTime.now()
    }

    constructor(latitude: Double, longitude: Double){
        this.latitude = latitude
        this.longitude = longitude
        this.time = LocalDateTime.now()
    }

    constructor(latitude: Double, longitude: Double, elevation:Double, time:LocalDateTime){
        this.latitude = latitude
        this.longitude = longitude
        this.elevation = elevation
        this.time = time
    }

    constructor(latitude: Double, longitude: Double, elevation:Double, time:Long){
        this.latitude = latitude
        this.longitude = longitude
        this.elevation = elevation
        this.time = long_to_time(time)
    }

    constructor(latitude: Double, longitude: Double, time:Long){
        this.latitude = latitude
        this.longitude = longitude
        this.time = long_to_time(time)
    }

    constructor(latitude: Double, longitude: Double, time:LocalDateTime){
        this.latitude = latitude
        this.longitude = longitude
        this.time = time
    }

    companion object {
        @JvmStatic
        fun long_to_time(time:Long):LocalDateTime{
            return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(time),
                TimeZone.getDefault().toZoneId()
            )
        }

        @JvmStatic
        fun time_to_long(time:LocalDateTime):Long{
            return time.toInstant(ZoneOffset.of(OffsetDateTime.now().offset.toString())).toEpochMilli()
        }
    }
}

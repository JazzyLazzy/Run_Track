package com.lazarus.run_track1

import SimpleGPX.TrackPoint
import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDateTime


class ParcelableTrackPoint : TrackPoint, Parcelable {

    constructor(latitude:Double, longitude:Double, elevation:Double, time: Long) :
            super(latitude, longitude, elevation, time)

    constructor(latitude:Double, longitude:Double, elevation:Double, time: LocalDateTime) :
            super(latitude, longitude, elevation, time)

    private constructor(parcel: Parcel) :
            super(parcel.readDouble(), parcel.readDouble(), parcel.readDouble(), parcel.readLong())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeDouble(elevation)
        parcel.writeLong(time_to_long(time))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableTrackPoint> {
        override fun createFromParcel(parcel: Parcel): ParcelableTrackPoint {
            return ParcelableTrackPoint(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableTrackPoint?> {
            return arrayOfNulls(size)
        }
    }

}
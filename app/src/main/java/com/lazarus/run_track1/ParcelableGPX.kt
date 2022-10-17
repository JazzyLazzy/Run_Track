package com.lazarus.run_track1

import android.os.Parcel
import android.os.Parcelable
import SimpleGPX.GPX;
import SimpleGPX.GPXWaypoint
import SimpleGPX.trk

class ParcelableGPX : GPX, Parcelable {

    constructor(creator:String, version:String) : super(creator, version)

    constructor(creator: String, version: String, waypoints: ArrayList<GPXWaypoint>, tracks:ArrayList<trk>) :
            super(creator, version, waypoints, tracks)

    private constructor(parcel: Parcel) : super()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableGPX> {
        override fun createFromParcel(parcel: Parcel): ParcelableGPX {
            return ParcelableGPX(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableGPX?> {
            return arrayOfNulls(size)
        }
    }

}

fun createParcelableGPX(gpx:GPX):ParcelableGPX{
    return ParcelableGPX(gpx.creator,gpx.version,gpx.waypoints,gpx.tracks)
}

fun createGPXFromParcelable(parcelableGPX: ParcelableGPX):GPX{
    return GPX(parcelableGPX.creator, parcelableGPX.version, parcelableGPX.waypoints, parcelableGPX.tracks)
}
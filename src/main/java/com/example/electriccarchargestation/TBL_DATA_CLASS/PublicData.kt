package com.example.electriccarchargestation.TBL_DATA_CLASS

import java.io.Serializable

data class PublicData(
    val id: Int?,
    val stationId: String?,
    val chargeAmount: String?,
    val chargeType: Int?,
    val parkingFree: String?,
    val stationName: String?,
    val address: String?,
    val latitude: Float?,
    val longtitude: Float?,
    var bookmark: Int?): Serializable
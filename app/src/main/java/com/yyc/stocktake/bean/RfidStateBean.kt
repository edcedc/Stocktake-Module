package com.yyc.stocktake.bean

data class RfidStateBean(

    val tagId: String? = null,

    val scanStatus: Int = 0,

    val rssi: String? = null

)
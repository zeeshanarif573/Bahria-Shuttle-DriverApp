package com.bahria.driverapp.ui.main.model.buses

data class BusesData(
    val CREATED_BY: String,
    val CREATED_ON: String,
    val IS_ACTIVE: String,
    val REG_ID: Double,
    val REG_NO: String
){
    override fun toString(): String {
        return REG_NO
    }
}
package com.bahria.driverapp.ui.main.model.routes

data class RoutesData(
    val CREATED_BY: String,
    val CREATED_ON: String,
    val IS_ACTIVE: String,
    val ROUTE_ID: Int,
    val ROUTE_NM: String
) {
    override fun toString(): String {
        return ROUTE_NM
    }
}
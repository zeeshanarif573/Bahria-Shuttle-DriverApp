package com.bahria.driverapp.config

import com.bahria.driverapp.ui.login.model.LoginResponse
import com.bahria.driverapp.ui.main.model.GeneralResponse
import com.bahria.driverapp.ui.main.model.buses.BusesResponse
import com.bahria.driverapp.ui.main.model.routes.RoutesResponse
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.*

interface API {

    @FormUrlEncoded
    @POST("APILOGINDRIVER/api/ApiLoginDriver/CheckDriverLogin")
    suspend fun login(
        @Field("DRIVER_ID") driverID: String,
        @Field("DRIVER_CNIC") driverCnic: String
    ): Response<LoginResponse>

    @GET("APILOGINDRIVER/api/GetAllRoutes/GetAllRoutes")
    suspend fun getRoutes(): Response<RoutesResponse>

    @GET("APILOGINDRIVER/api/APIBusByStatus/GetBusesByStatus")
    suspend fun getBuses(
        @Query("IS_ACTIVE") isActive: String,
    ): Response<BusesResponse>

    @FormUrlEncoded
    @POST("APILOGINDRIVER/api/ApiStartTripLogs/SendStartLogsData")
    suspend fun postDriverInformation(
        @Field("DRIVER_EMP_ID") driverEmpID: String,
        @Field("BUS_REG_ID") busRegID: String,
        @Field("ROUTE_ID") routeID: String,
        @Field("IS_ACTIVE") isActive: String
    ): Response<GeneralResponse>

    @FormUrlEncoded
    @POST("APILOGINDRIVER/api/ApiSendHistory/SendStopLogsData")
    suspend fun journeyEnd(
        @Field("IS_ACTIVE:N") isActive: String,
        @Field("DRIVER_EMP_ID") empID: String,
    ): Response<GeneralResponse>

}
package com.droidblossom.archive.data.source.remote.api

import com.droidblossom.archive.data.dto.ResponseBody
import com.droidblossom.archive.data.dto.auth.response.HealthResponseDto
import com.droidblossom.archive.data.dto.member.request.FcmTokenRequsetDto
import com.droidblossom.archive.data.dto.member.request.MemberDetailUpdateRequestDto
import com.droidblossom.archive.data.dto.member.request.MemberStatusRequestDto
import com.droidblossom.archive.data.dto.member.request.NotificationEnabledRequestDto
import com.droidblossom.archive.data.dto.member.response.MemberDetailResponseDto
import com.droidblossom.archive.data.dto.member.response.MemberStatusResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface MemberService {

    @GET("me")
    suspend fun getMeApi(): Response<ResponseBody<MemberDetailResponseDto>>

    @POST("me/status")
    suspend fun postMeStatusApi(
        @Body checkStatusRequestDto: MemberStatusRequestDto
    ) : Response<ResponseBody<MemberStatusResponseDto>>

//    @PATCH("me/")
//    suspend fun patchMeApi(
//        @Body request : MemberDetailUpdateRequestDto
//    ): Response<ResponseBody<HealthResponseDto>>

    @PATCH("me/notification_enabled")
    suspend fun patchNotificationEnabled(
        @Body request : NotificationEnabledRequestDto
    ): Response<ResponseBody<String>>

    @PATCH("me/fcm_token")
    suspend fun patchFcmToken(
        @Body request : FcmTokenRequsetDto
    ): Response<ResponseBody<String>>


    @GET("health")
    suspend fun getTextApi() : Response<ResponseBody<HealthResponseDto>>
}
package com.example.support.new_api

import com.example.support.new_model.*
import com.v2ray.rocket.new_model.ResetPasswordModel
import retrofit2.Call
import retrofit2.http.*

interface Routes {


    @GET("/eshtrak/list.php")
    fun getlist(): Call<PriceListModel?>?

    @GET("/eshtrak/chkuser.php")
    fun checkUser(@Query("token") token: String?): Call<CheckUserModel>

    @FormUrlEncoded
    @POST("/eshtrak/login.php")
    fun login(
        @Field("username") username: String?,
        @Field("password") password: String?,
        @Field("tokenfirebase") tokenfirebase: String?,
    ): Call<LoginModel>


    @FormUrlEncoded
    @POST("/eshtrak/reg.php")
    fun register(
        @Field("tokenfirebase") tokenfirebase: String?,
        @Field("name") name: String?,
        @Field("famili") family: String?,
        @Field("username") username: String?,
        @Field("password") password: String?,
        @Field("mobile") mobile: String?,
        @Field("uploaded_image_url") uploaded_image_url: String,
    ): Call<RegisterModel>


    @FormUrlEncoded
    @POST("/eshtrak/resetpassword.php")
    fun resetpassword(
        @Field("tokenfirebase") tokenfirebase: String?,
        @Field("username") username: String?,
        @Field("security_question") security_question: String?,
        @Field("new_password") new_password: String?,
        @Field("is_change_pass") is_change_pass: Boolean?,
    ): Call<ResetPasswordModel>

    @FormUrlEncoded
    @POST("/eshtrak/pay_off_code.php")
    fun checkOffCode(
        @Field("token") token: String?,
        @Field("idvip") idvip: String?,
        @Field("offcode") offcode: String?
    ): Call<OffCodeModel>

    @FormUrlEncoded
    @POST("/eshtrak/change_profile.php")
    fun changeProfile(
        @Field("token") token: String?,
        @Field("profile") profile: String?,
    ): Call<ChangeProfileModel>

    @GET("/eshtrak/pay_ref_code.php")
    fun checkReferralCode(
        @Query("token") token : String? = null,
       @Query("idvip") idVip : String? = null,
        @Query("offcode") offCode : String? = null,
        @Query("referralCode") referralCode : String? = null
    ) : Call<CheckReferralCodeResponse>

    @FormUrlEncoded
    @POST("/eshtrak/reg_active_code.php")
    fun confirmCode(
        @Field("mobile") phone : String,
        @Field("active_code") code : String
    ) : Call<ResponseActiveCode>

    @FormUrlEncoded
    @POST("/eshtrak/get_active_code.php")
    fun resendCode(
        @Field("mobile") phone : String
    ) : Call<ResponseActiveCode>

    @GET("/eshtrak/login_sms.php")
    fun loginOtp(
        @Query("mobile") mobile: String? = null,
        @Query("active_code") code: String? = null,
        @Query("tokenfirebase") tokenfirebase: String? = null,
    ): Call<LoginModel>

    @FormUrlEncoded
    @POST("/eshtrak/reg_reset_password.php")
    fun resetPasswordCode(
        @Field("mobile") phone : String? = null,
        @Field("active_code") code : String? = null,
        @Field("password") password : String? = null
    ) : Call<LoginModel>

    @GET("/eshtrak/settings.json")
    fun getSettings(): Call<AppSettingsModel>

    @GET("/appconnection/updateapplication.php")
    fun getUpdateLinks(): Call<List<DataModel>>

}
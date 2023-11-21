package com.example.support.new_api

import com.example.support.new_model.*
import com.example.support.viewpager2.VideoReactionModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface PanelRoutes {

    @GET("/api/tickets")
    fun getTickets(@Query("token") token: String?): Call<TicketsModel>

    @GET("/api/tickets/show")
    fun getTicketMessages(
        @Query("token") token: String?,
        @Query("ticket_id") ticket_id: Int?,
        @Query("is_visit") is_visit: Int?
    ): Call<TicketMessagesModel>

    @Multipart
    @POST("/api/tickets/add_message")
    fun addMessage(
        @Part("token") token: RequestBody?,
        @Part("ticket_status") ticket_status:RequestBody?,
        @Part("comment") comment: RequestBody?,
        @Part image : MultipartBody.Part?,
        @Part("ticket_id") ticket_id: RequestBody?
    ): Call<AddTicketModel?>?

    @FormUrlEncoded
    @POST("/api/tickets/add_ticket")
    fun addTicket(
        @Field("token") token: String?,
        @Field("title") title: String?
    ): Call<AddTicketModel>


    @GET("/api/tickets/change/ticket")
    fun checkTicket(
        @Query("token") token: String?,
        @Query("is_refresh") is_refresh: Int?
    ): Call<TicketChangeModel?>?


    @GET("/api/tickets/change/ticket/message")
    fun checkTicketMessage(
        @Query("token") token: String?,
        @Query("ticket_id") ticket_id: Int?,
        @Query("is_refresh") is_refresh: Int?
    ): Call<TicketChangeMessageModel?>?


    @Multipart
    @POST("/eshtrak/upload_profile.php")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part?
    ): UploadImageModel?

    @GET("api/admin/activity")
    fun checkAdminOnline() : Call<OnlineAdminResponse>

    @GET("api/user/walletAmount")
    fun getWalletAmount(
        @Query("token")  token : String? = null
    ) : Call<WalletAmountResponseModel>

    @GET("api/user/withDraw/store")
    fun store(
        @Query("token") token : String? = null,
        @Query("amount") wallet : String? = null,
        @Query("card_number") cardNumber : String? = null,
        @Query("card_owener") cardName : String? = null
    ) : Call<StoreResponseModel>

    @GET("api/user/withDraws")
    fun getUserDraws(
        @Query("token") token : String? = null
    ) : Call<ListUserWithDrawResponse>

    @GET("api/user/setting")
    fun getAtLeastPrice(
        @Query("token") token : String? = null
    ) : Call<AtLeastModel>

    @GET("api/videos")
    fun getVideos(@Query("token")token:String?): Call<GetVideoModel>

    @FormUrlEncoded
    @POST("api/videos/reaction")
    fun setReaction(
        @Field("token") token: String?,
        @Field("type") type: String?,
        @Field("video_id") video_id: Int?,
    ): Call<VideoReactionModel>

    @GET("api/stories")
    fun getStories(
        @Query("token") token : String? = null
    ) : Call<ListStories>

    @GET("api/stories/show")
    fun showStory(
        @Query("token") token : String? = null,
        @Query("story_id") id : Int? = null
    ) : Call<GetStory>

    @FormUrlEncoded
    @POST("api/stories/reaction")
    fun setStoryReaction(
        @Field("token") token : String? = null,
        @Field("story_gallery_id") galleryId : Int? = null,
        @Field("type") type : String? = null
    ) : Call<StoryReaction>
}
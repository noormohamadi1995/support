package com.example.support.new_api

import retrofit2.Retrofit
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory

object ApiPanelController {
    val client: Retrofit
        get() {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            return Retrofit.Builder()
                .baseUrl("https://panel.project-app.ir/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
}
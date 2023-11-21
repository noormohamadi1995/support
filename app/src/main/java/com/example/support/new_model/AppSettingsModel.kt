package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class AppSettingsModel (
    @SerializedName("app_link")
    var link: String? = null
)
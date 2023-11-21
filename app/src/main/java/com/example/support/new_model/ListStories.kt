package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class ListStories(
    @SerializedName("status") val status : String? = null,
    @SerializedName("stories") val stories : List<Story> = listOf()
)

data class Story(
    @SerializedName("id") val id : Int? = null,
    @SerializedName("title") val title : String? = null,
    @SerializedName("description") val description : String? = null,
    @SerializedName("sort") val sort : Int? = null,
    @SerializedName("status") val status : String? = null,
    @SerializedName("created_at") val createdAt : String? = null,
    @SerializedName("updated_at") val updatedAt : String? = null,
    @SerializedName("story_gallery") val storyGalleries : List<StoryGallery> = listOf()
)

data class StoryGallery(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("story_id") val storyId : Int? = null,
    @SerializedName("story_url") val storyUrl : String? = null,
    @SerializedName("image_url") val imageUrl : String? = null,
    @SerializedName("web") val web :String? = null,
    @SerializedName("likes") val likes : Int? = null,
    @SerializedName("views") val views : Int? = null,
    @SerializedName("is_like") val isLike : Boolean? = null,
    @SerializedName("is_view") val isView : Boolean? = null,
    @SerializedName("created_at") val createdAt : String? = null,
    @SerializedName("updated_at") val updated_at : String? = null
)

data class GetStory(
    @SerializedName("status") val status : String? = null,
    @SerializedName("story") val story: Story? = null,
    @SerializedName("is_like") val isLike : Boolean? = null,
    @SerializedName("is_view") val isView : Boolean? = null
)

data class StoryReaction(
    @SerializedName("status") val status : String? = null,
    @SerializedName("message") val message : String? = null
)

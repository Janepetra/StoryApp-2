package com.storyapp.response

import com.google.gson.annotations.SerializedName

data class AddStory(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

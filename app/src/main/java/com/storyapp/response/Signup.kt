package com.storyapp.response

import com.google.gson.annotations.SerializedName

data class Signup(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

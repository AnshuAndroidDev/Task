package com.example.interviewtask.model


import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("page")
    var page: Int,
    @SerializedName("per_page")
    var perPage: Int,
    @SerializedName("total")
    var total: Int,
    @SerializedName("total_pages")
    var totalPages: Int,
    @SerializedName("data")
    var data: List<UserData>,
    @SerializedName("support")
    var support: Support
) {
    data class UserData(
        @SerializedName("id")
        var id: Int,
        @SerializedName("email")
        var email: String,
        @SerializedName("first_name")
        var firstName: String,
        @SerializedName("last_name")
        var lastName: String,
        @SerializedName("avatar")
        var avatar: String
    )

    data class Support(
        @SerializedName("url")
        var url: String,
        @SerializedName("text")
        var text: String
    )
}

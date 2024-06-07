package com.JoelClarke.ybsmobilechallenge.networking.entity

import com.google.gson.annotations.SerializedName

class UserProfile(
    var id : String,
    var nsid : String,
    @SerializedName("join_date")
    var joinDate : Long,
    var occupation : String,
    var hometown : String,
    @SerializedName("first_name")
    var firstName : String,
    @SerializedName("last_name")
    var lastName : String,
    @SerializedName("profile_description")
    var description : String,
    var city : String,
    var country : String
) {

    fun getFullName() : String {
        var fName = ""
        firstName?.let {
            fName = it
        }

        var lName = ""
        lastName?.let {
            lastName = it
        }

        return "$fName $lName"
    }
}
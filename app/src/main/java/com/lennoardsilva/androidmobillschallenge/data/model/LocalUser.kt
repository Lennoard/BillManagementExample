package com.lennoardsilva.androidmobillschallenge.data.model

import com.google.firebase.auth.FirebaseUser
import com.lennoardsilva.androidmobillschallenge.utils.Utils
import java.io.Serializable

data class LocalUser(
    var uid: String = Utils.randomUid(),
    var displayName: String = DEFAULT_USERNAME,
    var email : String = "",
    var photoUrl: String = DEFAULT_PHOTO_URL,
    var messagingToken: String? = null,
    var joinTime: Long = 0
) : Serializable {

    fun toMap(): Map<String, Any> {
        return mutableMapOf<String, Any>(
            "uid" to uid,
            "displayName" to displayName,
            "email" to email,
            "photoUrl" to photoUrl
        ).apply {
            if (joinTime > 0) {
                put("joinTime", joinTime)
            }
            messagingToken?.let {
                put("messagingToken", it)
            }
        }
    }

    companion object {
        const val DEFAULT_USERNAME = "Novo usuário"
        const val DEFAULT_PHOTO_URL = "https://firebasestorage.googleapis.com/v0/b/bill-management-3b89a.appspot.com/o/internal%2Fdefault-profile-pic.png?alt=media&token=e77e205c-671f-4c36-97d2-c823fd17df9b"

        fun createFromFirebaseUser(user: FirebaseUser) : LocalUser {
            return LocalUser(
                uid = user.uid,
                displayName = user.displayName ?: DEFAULT_USERNAME,
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: DEFAULT_PHOTO_URL,
                joinTime = System.currentTimeMillis()
            )
        }
    }

}
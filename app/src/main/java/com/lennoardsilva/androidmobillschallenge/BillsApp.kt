package com.lennoardsilva.androidmobillschallenge

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lennoardsilva.androidmobillschallenge.data.Consts
import com.lennoardsilva.androidmobillschallenge.data.model.LocalUser

class BillsApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }

    companion object {
        private var account: LocalUser? = null
        private val userSpace : String
            get() = "users/${findCurrentAccount().uid}"

        private val db : FirebaseFirestore by lazy {
            Firebase.firestore
        }

        val auth : FirebaseAuth by lazy {
            FirebaseAuth.getInstance()
        }

        val isLoggedIn: Boolean
            get() = auth.currentUser != null

        val userRef : DocumentReference
            get() = db.document(userSpace)

        val userCostsRef : CollectionReference
            get() = db.collection("$userSpace/${Consts.DB_COSTS}")

        val userRevenuesRef : CollectionReference
            get() = db.collection("$userSpace/${Consts.DB_REVENUES}")

        fun findCurrentAccount(): LocalUser {
            if (account != null) return account as LocalUser

            val firebaseUser = auth.currentUser
            return if (firebaseUser != null) {
                LocalUser.createFromFirebaseUser(firebaseUser).also {
                    account = it
                }
            } else LocalUser().apply {
                displayName = "Convidado"
                joinTime = System.currentTimeMillis()
            }
        }
    }
}
package com.example.firebase_chat_demo.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.example.firebase_chat_demo.data.model.chat.Message
import com.example.firebase_chat_demo.data.model.user.User
import com.example.firebase_chat_demo.data.response.DataResponse
import com.example.firebase_chat_demo.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class HomeViewModel(val application: Application) : ViewModel() {

    private var mFirebaseUser: FirebaseUser? = null
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabaseReferenceMessages: DatabaseReference? = null
    private var currentUserLiveData = MutableLiveData<DataResponse<User>>()
    var usersLiveData = MutableLiveData<DataResponse<MutableList<User>>>()
    private var messageList = mutableListOf<Message>()
    private var userList = mutableListOf<User>()

    init {
        mFirebaseUser = FirebaseAuth.getInstance().currentUser
        mDatabaseReference =
            FirebaseDatabase.getInstance().getReference(Constants.USERS_TABLE)
                .child(mFirebaseUser!!.uid)
        mDatabaseReferenceMessages =
            FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_TABLE)
                .child(mFirebaseUser!!.uid)
        usersLiveData.value = DataResponse.DataEmptyResponse()
    }

    fun getCurrentUser() {
        mDatabaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUserLiveData.value =
                    DataResponse.DataSuccessResponse(snapshot.getValue(User::class.java)!!)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun getChatList() {
        mDatabaseReferenceMessages!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (ds in snapshot.children) {
                    val message = ds.getValue(Message::class.java)
                    if (message != null) {
                        messageList.add(message)
                    }
                }
                val databaseReference =
                    FirebaseDatabase.getInstance().getReference(Constants.USERS_TABLE)
                databaseReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userList.clear()
                        for (i in snapshot.children) {
                            val user = i.getValue(User::class.java)
                            for (j in messageList) {
                                if (user!!.id == j.id) {
                                    userList.add(user)
                                }
                            }
                        }
                        usersLiveData.value = DataResponse.DataSuccessResponse(userList)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    val imageUrl: LiveData<String> = Transformations.map(currentUserLiveData) {
        (it as DataResponse.DataSuccessResponse).body.imageURL
    }

    val userName: LiveData<String> = Transformations.map(currentUserLiveData) {
        (it as DataResponse.DataSuccessResponse).body.username
    }


    class Factory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
package com.ozanarik.mvvmcontacts.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val auth: FirebaseAuth) :ViewModel() {

    private val _signUpResult:MutableStateFlow<Resource<Unit>> = MutableStateFlow(Resource.Loading())
    val signUpResult:StateFlow<Resource<Unit>> = _signUpResult


    fun signUp(email:String,password:String){

        if(email.isEmpty() || password.isEmpty()){

            _signUpResult.value = Resource.Error("Password or e-mail must not be empty")
        }else if(email.isNotEmpty() && password.isNotEmpty()){
            signUpUser(email, password)
        }
    }

    fun signUpUser(email:String,password:String):Resource<Unit> {


        try {

            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {

                _signUpResult.value = Resource.Success(Unit)
            }.addOnFailureListener {

                _signUpResult.value = Resource.Error(it.localizedMessage!!)
            }
        }catch (e:Exception){

            _signUpResult.value = Resource.Error(e.localizedMessage!!)
        }


        return Resource.Success(Unit)

    }

}
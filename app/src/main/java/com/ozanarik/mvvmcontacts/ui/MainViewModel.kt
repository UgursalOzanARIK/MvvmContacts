package com.ozanarik.mvvmcontacts.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.storage.FirebaseStorage
import com.ozanarik.mvvmcontacts.model.Contacts
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val firebaseStorage:FirebaseStorage, private val firestore: FirebaseFirestore,private val auth: FirebaseAuth) :ViewModel() {

    private val _signUpResult:MutableStateFlow<Resource<Unit>> = MutableStateFlow(Resource.Loading())
    val signUpResult:StateFlow<Resource<Unit>> = _signUpResult

    private val _uploadPhotoState:MutableStateFlow<Resource<Unit>> = MutableStateFlow(Resource.Loading())
    val uploadPhotoState:StateFlow<Resource<Unit>> = _uploadPhotoState

    private val _uploadContactToFireStoreState:MutableStateFlow<Resource<Unit>> = MutableStateFlow(Resource.Loading())
    val uploadContactToFireStoreState:StateFlow<Resource<Unit>> = _uploadContactToFireStoreState

    private val _readFireStoreDataState:MutableStateFlow<Resource<List<Contacts>>> = MutableStateFlow(Resource.Loading())
    val readFireStoreDataState:StateFlow<Resource<List<Contacts>>> = _readFireStoreDataState

    private val _deleteFromFireStoreStateFlow:MutableStateFlow<Resource<Unit>> = MutableStateFlow(Resource.Loading())
    val deleteFromFireStoreStateFlow:StateFlow<Resource<Unit>> = _deleteFromFireStoreStateFlow




    fun signUp(email:String,password:String)=viewModelScope.launch{

        if(email.isEmpty() || password.isEmpty()){

            _signUpResult.value = Resource.Error("Password or e-mail must not be empty")
        }else if(email.isNotEmpty() && password.isNotEmpty()){
            signUpUser(email, password)
        }
    }

    fun uploadPhotoToFirebaseStorage(selectedImg:Uri):Resource<Unit> {


        val userRef = firebaseStorage.reference
        val randomUID = UUID.randomUUID()

        val imageName = "${auth.currentUser!!.email} $randomUID.png"

        val imgRef = userRef.child("images/").child(imageName)

        imgRef.putFile(selectedImg).addOnSuccessListener {


            imgRef.downloadUrl.addOnSuccessListener {

                val downloadUrl = it.toString()

                val currentUser = auth.currentUser
                val currentUserUID = currentUser?.uid

                Log.e("asd","sssss")

       /*         if(currentUserUID!=null){

                    val contactMap = hashMapOf<String,Any>()

                    contactMap["downloadUrl"] = downloadUrl
                    contactMap["timeStamp"] = Timestamp.now()


                    val collection = firestore.collection("Users").document(currentUserUID)

                    collection.collection("Contacts").add(contactMap).addOnSuccessListener {
                       Log.e("asd","helal")
                    }.addOnFailureListener {e->
                        Log.e("asd",e.localizedMessage!!)
                    }
                }*/
            }


            _uploadPhotoState.value = Resource.Success(Unit)
        }.addOnFailureListener{
            _uploadPhotoState.value = Resource.Error(it.localizedMessage!!)
        }

        return Resource.Loading()
    }

    private fun signUpUser(email:String, password:String):Resource<Unit> {

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

fun uploadContactToFireStore(contactName:String, contactPhoneNumber:String):Resource<Unit>{
    try {
        val currentUserUID = auth.currentUser?.uid

        if(currentUserUID!=null){

            val userRef = firestore.collection("Users").document(currentUserUID)
            val contactMap = hashMapOf<String,Any>()


            val timeUploaded = Timestamp.now()

            contactMap["contactName"] = contactName
            contactMap["contactPhoneNumber"] = contactPhoneNumber
            contactMap["timeUploaded"] = timeUploaded
            contactMap["currentUserEmail"] = auth.currentUser!!.email!!

            userRef.collection("Contacts").add(contactMap).addOnSuccessListener {

                Log.e("asd","successfully created")
                _uploadContactToFireStoreState.value = Resource.Success(Unit)

            }.addOnFailureListener {
                Log.e("asd", it.localizedMessage!!)
                _uploadContactToFireStoreState.value = Resource.Error(it.localizedMessage!!)
            }
        }

    }catch (e:Exception) {

        return Resource.Error(e.localizedMessage!!)

    }
    return Resource.Loading()
    }

    fun readFireStoreContactData()=viewModelScope.launch {

        val currentUser = auth.currentUser
        val currentUserUID = currentUser?.uid

        val contactList = mutableListOf<Contacts>()
        try {
            if (currentUserUID!=null){

                    val userRef = firestore.collection("Users").document(currentUserUID)

                    userRef.collection("Contacts").addSnapshotListener(MetadataChanges.INCLUDE){ value, error->

                        if (value!=null){

                            val newContactList = mutableListOf<Contacts>()

                            val contacts = value.documents

                            for (c in contacts){

                                val contactName = c.get("contactName")as String
                                val contactPhoneNumber = c.get("contactPhoneNumber")as String

                                val newContact=Contacts(0,contactName,contactPhoneNumber)

                                newContactList.add(newContact)
                            }

                            //TO ACCESS THE FIRESTORE CONTACTS DATA ONLY ONCE AT A TIME, PREVENTING DUPLICATION AND UNNECESSARY LISTING ON RECYCLERVIEW
                            synchronized(contactList){
                                contactList.clear()
                                contactList.addAll(newContactList)
                            }
                            _readFireStoreDataState.value = Resource.Success(contactList)
                        }
                    }
                }
            }catch (e:Exception){
                _readFireStoreDataState.value = Resource.Error(e.localizedMessage!!)
            }
    }


    fun deleteFireStoreContact(contactName:String,contactPhoneNumber: String) = viewModelScope.launch {

        val currentUser = auth.currentUser
        val currentUserUID = currentUser?.uid


        if(currentUserUID!=null){
            try {
                val userRef = firestore.collection("Users").document(currentUserUID)

                userRef.collection("Contacts")
                    .whereEqualTo("contactName",contactName)
                    .whereEqualTo("contactPhoneNumber",contactPhoneNumber)
                    .get()
                    .addOnSuccessListener {querySnapshot->

                        for (document in querySnapshot ){

                            document.reference.delete()
                            Log.e("success","deleted ${document.id} : $contactName")
                            _deleteFromFireStoreStateFlow.value = Resource.Success(Unit)
                        }
                    }.addOnFailureListener {exception->

                        _deleteFromFireStoreStateFlow.value = Resource.Error(exception.localizedMessage!!)
                    }
            }catch (e:Exception){
                _deleteFromFireStoreStateFlow.value = Resource.Error(e.localizedMessage!!)

            }catch (e:FirebaseFirestoreException){
                _deleteFromFireStoreStateFlow.value = Resource.Error(e.localizedMessage!!)

            }catch (e:IOException){
                _deleteFromFireStoreStateFlow.value = Resource.Error(e.localizedMessage!!)
            }
        }







    }

}

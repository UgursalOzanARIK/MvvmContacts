package com.ozanarik.mvvmcontacts.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
class MainViewModel @Inject constructor(private val firestore: FirebaseFirestore,private val auth: FirebaseAuth) :ViewModel() {

    private val _signUpResult:MutableStateFlow<Resource<Unit>> = MutableStateFlow(Resource.Loading())
    val signUpResult:StateFlow<Resource<Unit>> = _signUpResult

    private val _uploadContactToFireStoreState:MutableStateFlow<Resource<Unit>> = MutableStateFlow(Resource.Loading())
    val uploadContactToFireStoreState:StateFlow<Resource<Unit>> = _uploadContactToFireStoreState

    private val _readFireStoreDataState:MutableStateFlow<Resource<List<Contacts>>> = MutableStateFlow(Resource.Loading())
    val readFireStoreDataState:StateFlow<Resource<List<Contacts>>> = _readFireStoreDataState

    private val _deleteFromFireStoreStateFlow:MutableStateFlow<Resource<Unit>> = MutableStateFlow(Resource.Loading())
    val deleteFromFireStoreStateFlow:StateFlow<Resource<Unit>> = _deleteFromFireStoreStateFlow

    private val _searchState:MutableStateFlow<Resource<List<Contacts>>> = MutableStateFlow(Resource.Loading())
    val searchState:StateFlow<Resource<List<Contacts>>> = _searchState

    private val _updateState:MutableStateFlow<Resource<Contacts>> = MutableStateFlow(Resource.Loading())
    val updateState:StateFlow<Resource<Contacts>> = _updateState

    private val _signOutState:MutableStateFlow<Resource<Unit>> = MutableStateFlow(Resource.Loading())
    val signOutState:StateFlow<Resource<Unit>> = _signOutState

    private val _signInState:MutableStateFlow<Resource<Unit>> = MutableStateFlow(Resource.Loading())
    val signInState:StateFlow<Resource<Unit>> = _signInState


    fun signUp(email:String,password:String)=viewModelScope.launch{

        if(email.isEmpty() || password.isEmpty()){

            _signUpResult.value = Resource.Error("Password or e-mail must not be empty")
        }else if(email.isNotEmpty() && password.isNotEmpty()){
            signUpUser(email, password)
        }
    }

    fun signOutUser()=viewModelScope.launch {

        val currentUser=auth.currentUser

        if(currentUser!=null){
            auth.signOut()
            _signOutState.value = Resource.Success(Unit)
        }else {
            _signOutState.value=Resource.Error("Error Occured")
        }
    }


    fun signIn(email:String,password: String){

        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {signInResult->

            _signInState.value = Resource.Success(Unit)
        }.addOnFailureListener { signInResult->
            _signInState.value = Resource.Error(signInResult.localizedMessage!!)

        }


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

fun uploadContactToFireStore(contactName:String, contactPhoneNumber:String){

    try {

        val currentUser= auth.currentUser
        val currentUserUid = currentUser?.uid

        val contactHashmap = hashMapOf<String,Any>()
        val timeUploaded = Timestamp.now()

        if (currentUserUid!=null){

            contactHashmap["contactName"] = contactName
            contactHashmap["contactPhoneNumber"] = contactPhoneNumber
            contactHashmap["timeUploaded"] = timeUploaded
            contactHashmap["currentUserEmail"] = currentUser.email!!

            val userRef = firestore.collection("Users").document(currentUserUid)

            userRef.collection("Contacts").add(contactHashmap).addOnSuccessListener { success->

                _uploadContactToFireStoreState.value = Resource.Success(Unit)

            }.addOnFailureListener {error->
                _uploadContactToFireStoreState.value = Resource.Error(error.localizedMessage!!)
            }
        }
    }catch (e:Exception){
        _uploadContactToFireStoreState.value = Resource.Error(e.localizedMessage!!)
    }
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

                                val contactName = c.get("contactName")
                                val contactPhoneNumber = c.get("contactPhoneNumber")

                                if(contactName!=null && contactPhoneNumber!=null){
                                    val newContact=Contacts(0,contactName as String,contactPhoneNumber as String)
                                    newContactList.add(newContact)
                                }
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

    fun updateContact(oldContact: Contacts, newName:String, newPhoneNumber:String) = viewModelScope.launch {

        val currentUser = auth.currentUser
        val currentUserUID = currentUser?.uid

        if(currentUserUID!=null){

            try {

                val userRef = firestore.collection("Users").document(currentUserUID)

                val query = userRef.collection("Contacts")
                    .whereEqualTo("contactName",oldContact.name)
                    .whereEqualTo("contactPhoneNumber",oldContact.phoneNumber)
                    .get()
                    .addOnSuccessListener {querySnapshot->

                            if(querySnapshot!=null){

                                for (contact in querySnapshot){

                                    val contactHashmap = hashMapOf<String,Any>()
                                    contactHashmap["contactName"] = newName
                                    contactHashmap["contactPhoneNumber"] = newPhoneNumber

                                    userRef.collection("Contacts").document(contact.id).update(contactHashmap).addOnSuccessListener {
                                        Log.e("success","success! /// ${newName} updated")
                                    }.addOnFailureListener {
                                        Log.e("failure",it.localizedMessage!!)
                                    }
                                }
                            }
                    }

            }catch (e:Exception){
                _updateState.value = Resource.Error(e.localizedMessage!!)
            }catch (e:IOException){
                _updateState.value = Resource.Error(e.localizedMessage!!)
            }
        }
    }

    fun searchFireStoreContact(searchQuery:String) = viewModelScope.launch {

        val currentUser = auth.currentUser
        val currentUserUID = currentUser?.uid

        val contactList = mutableListOf<Contacts>()
        if(currentUserUID!=null){

           try {

               val userRef = firestore.collection("Users").document(currentUserUID)

               userRef.collection("Contacts").addSnapshotListener { value, error ->

                   if(error!=null){
                       _searchState.value = Resource.Error(error.localizedMessage!!)
                       Log.e("asd","error from snapshotListener")
                   }else if(value!=null){

                       val documents = value.documents

                       for (everyContact in documents){


                           val contactName = everyContact.get("contactName")
                           val contactPhoneNumber = everyContact.get("contactPhoneNumber")


                           if(contactName!=null && contactPhoneNumber!=null){
                               val newContact = Contacts(0,contactName as String,contactPhoneNumber as String)
                               contactList.add(newContact)

                               val filteredList = contactList.filter { query->
                                   query.toString().lowercase().contains(searchQuery)
                               }

                               Log.e("asd",filteredList.size.toString())

                               _searchState.value = Resource.Success(filteredList)

                               for (c in filteredList){
                                   Log.e("asd",c.name)
                               }
                           }
                       }
                   }
               }

           }catch (e:Exception){
               _searchState.value = Resource.Error(e.localizedMessage!!)
           }
        }
}

}

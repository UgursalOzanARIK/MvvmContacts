package com.ozanarik.mvvmcontacts.business.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.ozanarik.mvvmcontacts.model.Contacts
import com.ozanarik.mvvmcontacts.util.Resource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.IOException

class FirebaseRepository @Inject constructor(private val auth: FirebaseAuth, private val firebaseFirestore: FirebaseFirestore) {


    fun uploadContactToFireStore(contactName:String, contactPhoneNumber:String):Flow<Resource<Unit>> = flow{


        emit(Resource.Loading())

        val currentUser = auth.currentUser
        val currentUserUID = currentUser?.uid


        try {

            if(currentUserUID!=null){


                val userRef = firebaseFirestore.collection("Users").document(currentUserUID)

                val contactMap = hashMapOf<String,Any>()

                val timeUploaded = Timestamp.now()

                contactMap["contactName"] = contactName
                contactMap["contactPhoneNumber"] = contactPhoneNumber
                contactMap["timeUploaded"] = timeUploaded

                userRef.collection("Contacts").add(contactMap).await()


                emit(Resource.Success(Unit))

            }
        }catch (e:Exception){

            emit(Resource.Error(e.localizedMessage!!))

        }catch (e:FirebaseFirestoreException){
            emit(Resource.Error(e.localizedMessage!!))

        }
        catch (e:IOException){
            emit(Resource.Error(e.localizedMessage!!))

        }
    }

    fun readAllFireStoreContactData():Resource<List<Contacts>>{



        val currentUser=auth.currentUser
        val currentUserUID = currentUser?.uid

        try {
            val contactList = mutableListOf<Contacts>()

            if(currentUserUID!=null){

                val userRef = firebaseFirestore.collection("Users").document(currentUserUID)

                userRef.collection("Contacts").addSnapshotListener { value, error ->

                    if(error!=null){

                        Log.e("asd",error.localizedMessage!!)
                    }else if (value!=null){

                        val document = value.documents

                        for (everyContact in document){

                            val contactName = everyContact.get("contactName") as String
                            val contactPhoneNumber = everyContact.get("contactPhoneNumber") as String
                            val contact = Contacts(contactName,contactPhoneNumber)

                            contactList.add(contact)

                        }

                    }
                }
                return Resource.Success(contactList)

            }
        }catch (e:Exception){
            return  (Resource.Error(e.localizedMessage!!))
        }catch (e:FirebaseFirestoreException){
            return (Resource.Error(e.localizedMessage!!))

        }catch (e:IOException){
            return (Resource.Error(e.localizedMessage!!))
        }

        return Resource.Loading()
    }

}
package com.ozanarik.mvvmcontacts.business.repository

import com.ozanarik.mvvmcontacts.business.local.ContactsDB
import com.ozanarik.mvvmcontacts.model.Contacts
import com.ozanarik.mvvmcontacts.util.Resource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import java.sql.SQLException

class LocalRepository @Inject constructor(private val contactsDB: ContactsDB) {

    fun getAllContacts():Flow<Resource<List<Contacts>>> = flow{

        emit(Resource.Loading())
        try {

            val contactList = contactsDB.getContactsDao().getAllContacts()

            contactList.collect{allContacts->

                emit(Resource.Success(allContacts))
            }
        }catch (e:SQLException){
            emit(Resource.Error(e.localizedMessage!!))
        }catch (e:Exception){
            emit(Resource.Error(e.localizedMessage!!))
        }
    }

    suspend fun insertContact(contacts: Contacts) {

        contactsDB.getContactsDao().favoriteContact(contacts)
    }


}
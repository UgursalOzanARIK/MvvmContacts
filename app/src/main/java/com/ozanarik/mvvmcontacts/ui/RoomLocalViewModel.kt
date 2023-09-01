package com.ozanarik.mvvmcontacts.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ozanarik.mvvmcontacts.business.repository.LocalRepository
import com.ozanarik.mvvmcontacts.model.Contacts
import com.ozanarik.mvvmcontacts.util.DataStoreManager
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomLocalViewModel @Inject constructor (application: Application,private val localRepository: LocalRepository)

    :AndroidViewModel(application){

    private val _localContactsData:MutableStateFlow<Resource<List<Contacts>>> = MutableStateFlow(Resource.Loading())
    val localContactsData:StateFlow<Resource<List<Contacts>>> = _localContactsData

    val dataStoreManager = DataStoreManager(application)


    private val _isFav:MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isFav:StateFlow<Boolean> = _isFav


    fun getAllContacts()= viewModelScope.launch {


        localRepository.getAllContacts().collect{contactList->

            when(contactList){

                is Resource.Success->{
                    _localContactsData.value = Resource.Success(contactList.data!!)
                }
                is Resource.Loading->{
                    _localContactsData.value = Resource.Loading()
                }
                is Resource.Error->{
                    _localContactsData.value = Resource.Error(contactList.message!!)
                }
            }
        }
    }


    fun insertContact(contacts: Contacts) = viewModelScope.launch {

        localRepository.insertContact(contacts)
        _isFav.value = true

        val isFavouriteContact = _isFav.value

        dataStoreManager.setFavContact(isFavouriteContact)
    }




}
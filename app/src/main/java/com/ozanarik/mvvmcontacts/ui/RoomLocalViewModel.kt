package com.ozanarik.mvvmcontacts.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ozanarik.mvvmcontacts.business.repository.LocalRepository
import com.ozanarik.mvvmcontacts.model.Contacts
import com.ozanarik.mvvmcontacts.util.DatastoreManager
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomLocalViewModel @Inject constructor
                    (

        private val datastoreManager: DatastoreManager,
        private val localRepository: LocalRepository

                    )

    :ViewModel(){

    private val _localContactsData:MutableStateFlow<Resource<List<Contacts>>> = MutableStateFlow(Resource.Loading())
    val localContactsData:StateFlow<Resource<List<Contacts>>> = _localContactsData


    private val _isFav:MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isFav:StateFlow<Boolean> = _isFav

    fun saveFavContactBool(isFavContact:Boolean) = viewModelScope.launch {
        datastoreManager.saveFavContact(isFavContact)
    }

    fun getFavContactBool() = datastoreManager.getSavedFavContact().asLiveData(Dispatchers.IO)

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

    fun updateFavStatus(id:Long,isFav:Boolean) = viewModelScope.launch {
        localRepository.updateFavStatus(id,isFav)
    }


    fun insertContact(contacts: Contacts) = viewModelScope.launch {

        localRepository.insertContact(contacts)
    }

}
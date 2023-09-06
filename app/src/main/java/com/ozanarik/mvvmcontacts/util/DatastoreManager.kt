package com.ozanarik.mvvmcontacts.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DatastoreManager @Inject constructor (context:Context) {

    private val Context.dataStore:DataStore<Preferences> by preferencesDataStore("AppPrefs")
    private val dataStore = context.dataStore

    companion object{
        private val isFavContactKey = booleanPreferencesKey("isFavouriteContact")
    }

    suspend fun saveFavContact(isFavContactBool:Boolean){
        dataStore.edit { appPrefs->
            appPrefs[isFavContactKey] = isFavContactBool
            Log.e("asd","datastore saveledim")
        }
    }

    fun getSavedFavContact():Flow<Boolean>{

        return dataStore.data.catch { exception->
            if (exception is Exception){
                emit(emptyPreferences())
            }else{
                throw exception
            }
        }.map { appPrefs->
            val yourFavContact = appPrefs[isFavContactKey]?:false
            yourFavContact
        }
    }
}
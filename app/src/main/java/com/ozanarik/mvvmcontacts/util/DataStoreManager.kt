package com.ozanarik.mvvmcontacts.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreManager(context:Context) {

    private val Context.dataStore:DataStore<Preferences> by preferencesDataStore("AppPrefs")
    private val dataStore = context.dataStore

    companion object{

        @Volatile
        private var instance: DataStoreManager? = null

        operator fun invoke(context:Context): DataStoreManager{

            return instance ?: synchronized(this){
                instance?: DataStoreManager(context).also { instance = it }
            }
        }

        val isFavourite = booleanPreferencesKey("isFavContactKey")
    }

    suspend fun setFavContact(isFav:Boolean){
        dataStore.edit { appPrefs->
            appPrefs[isFavourite] = isFav
        }
    }

    fun getFavContact():Flow<Boolean>{
        return dataStore.data.catch { exception->
            if (exception is Exception){
                emit(emptyPreferences())
            }else{
                throw exception
            }
        }.map {appPrefs->

            val isFav = appPrefs[isFavourite]?:false
            isFav
        }
    }
}
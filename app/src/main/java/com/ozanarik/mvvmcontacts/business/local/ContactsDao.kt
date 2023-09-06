package com.ozanarik.mvvmcontacts.business.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ozanarik.mvvmcontacts.model.Contacts
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {

    @Query("SELECT * FROM CONTACTS_TABLE")
    fun getAllContacts(): Flow<List<Contacts>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun favoriteContact(contacts: Contacts)

    @Delete
    suspend fun deleteFromContacts(contacts: Contacts)



}
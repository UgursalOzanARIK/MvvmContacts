package com.ozanarik.mvvmcontacts.business.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ozanarik.mvvmcontacts.model.Contacts

@Database(
    entities = [Contacts::class],
    version = 2,
        )

abstract class ContactsDB:RoomDatabase() {

    abstract fun getContactsDao():ContactsDao
}
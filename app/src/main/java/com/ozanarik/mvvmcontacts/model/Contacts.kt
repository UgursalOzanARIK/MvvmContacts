package com.ozanarik.mvvmcontacts.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
@Entity(tableName = "contacts_table")

data class Contacts(
    @PrimaryKey(autoGenerate = true)
    val id:Long,
    val name: String,
    val phoneNumber:String
):Serializable

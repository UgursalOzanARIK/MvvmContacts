package com.ozanarik.mvvmcontacts.dependencyinjection

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ozanarik.mvvmcontacts.business.local.ContactsDB
import com.ozanarik.mvvmcontacts.business.repository.LocalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideAuth():FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFireStore():FirebaseFirestore=FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage():FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideRoomDB(@ApplicationContext context:Context):ContactsDB{

        return Room.databaseBuilder(context,ContactsDB::class.java,"contacts_db")
            .fallbackToDestructiveMigration()
            .build()
    }
    @Provides
    @Singleton
    fun provideLocalRoomRepo(contactsDB: ContactsDB):LocalRepository = LocalRepository(contactsDB)


}
package com.ozanarik.mvvmcontacts.dependencyinjection

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ozanarik.mvvmcontacts.business.repository.FirebaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideFireStore()=FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseRepository(auth:FirebaseAuth,firebaseFirestore: FirebaseFirestore):FirebaseRepository{

        return FirebaseRepository(auth,firebaseFirestore)


    }



}
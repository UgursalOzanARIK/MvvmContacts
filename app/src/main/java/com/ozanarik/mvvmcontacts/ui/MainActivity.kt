package com.ozanarik.mvvmcontacts.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.ActivityMainBinding
import com.ozanarik.mvvmcontacts.model.Contacts
import com.ozanarik.mvvmcontacts.ui.adapter.ContactsAdapter
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mainViewModel: MainViewModel
    private lateinit var contactsAdapter:ContactsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)
        handleRecyclerView()


        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        firestore = Firebase.firestore
        auth = Firebase.auth
        readFireStoreContacts()

        binding.floatingActionButton.setOnClickListener {


            //addContact
            AddContactDialogFragment().show(supportFragmentManager,AddContactDialogFragment().tag)

        }

    }


    private fun readFireStoreContacts(){

        mainViewModel.readFireStoreContactData()
        lifecycleScope.launch {

            mainViewModel.readFireStoreDataState.collect{resultData->
                when(resultData){
                    is Resource.Success->{
                        resultData.data?.let { contactsAdapter.differList.submitList(it) }
                        contactsAdapter.notifyDataSetChanged()
                        Log.e("asdas","got data")

                    }
                    is Resource.Loading->{
                        Log.e("asd","loading")
                    }
                    is Resource.Error->{
                        Toast.makeText(this@MainActivity,resultData.message,Toast.LENGTH_LONG).show()
                        Log.e("asd","asdasd")
                    }
                }
            }
        }
    }

    private fun handleRecyclerView(){
        contactsAdapter = ContactsAdapter()

        binding.contactRv.apply {

            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = contactsAdapter
            firestoreSettings { setRecyclerListener { binding.contactRv } }

        }
    }


    override fun onStart() {
        super.onStart()
        handleRecyclerView()
    }

}
package com.ozanarik.mvvmcontacts.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.ActivityMainBinding
import com.ozanarik.mvvmcontacts.ui.adapter.ContactsAdapter
import com.ozanarik.mvvmcontacts.util.AdapterItemClickListener
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsActivity : AppCompatActivity(){
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
                        resultData?.let { contactsAdapter.differList.submitList(it.data) }
                        contactsAdapter.notifyDataSetChanged()
                        Log.e("asdas","got data")

                    }
                    is Resource.Loading->{
                        Log.e("asd","loading")
                    }
                    is Resource.Error->{
                        Toast.makeText(this@ContactsActivity,resultData.message,Toast.LENGTH_LONG).show()
                        Log.e("asd","asdasd")
                    }
                }
            }
        }
    }

    private fun handleRecyclerView(){
        contactsAdapter = ContactsAdapter(object : AdapterItemClickListener {
            override fun onItemClick(position: Int) {

                val currentContact = contactsAdapter.differList.currentList[position]

                val intent = Intent(this@ContactsActivity,ContactDetailActivity::class.java)

                intent.putExtra("contactName",currentContact.name)
                intent.putExtra("contactPhoneNumber",currentContact.phoneNumber)

                startActivity(intent)
            }
        })

        binding.contactRv.apply {

            layoutManager = LinearLayoutManager(this@ContactsActivity)
            setHasFixedSize(true)
            adapter = contactsAdapter
            firestoreSettings { setRecyclerListener { binding.contactRv } }

        }
    }


    override fun onResume() {
        readFireStoreContacts()
        handleRecyclerView()
        super.onResume()
    }

}
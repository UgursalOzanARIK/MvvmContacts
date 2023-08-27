package com.ozanarik.mvvmcontacts.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.ActivityMainBinding
import com.ozanarik.mvvmcontacts.ui.adapter.ContactsAdapter
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var contactsAdapter:ContactsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)
        handleRecyclerView()


        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        binding.floatingActionButton.setOnClickListener {


            //addContact
            AddContactDialogFragment().show(supportFragmentManager,AddContactDialogFragment().tag)

        }


        getAllFireStoreContactData()


    }

    fun getAllFireStoreContactData(){

        mainViewModel.readAllFireStoreData()
        lifecycleScope.launch {
            mainViewModel.readFireStoreDataState.collect{readFireStoreDataState->
                when(readFireStoreDataState){
                    is Resource.Success->{
                        readFireStoreDataState.data?.let { contactsAdapter.differList.submitList(it) }
                        contactsAdapter.notifyDataSetChanged()

                    }
                    is Resource.Error->{
                        Snackbar.make(binding.contactRv,readFireStoreDataState.message!!,Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Loading->{
                        Snackbar.make(binding.contactRv,"Getting contacts",Snackbar.LENGTH_LONG).show()

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

        }



    }

}
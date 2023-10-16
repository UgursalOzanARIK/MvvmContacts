package com.ozanarik.mvvmcontacts.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.FragmentContactsBinding
import com.ozanarik.mvvmcontacts.ui.LoginSignUpActivity
import com.ozanarik.mvvmcontacts.ui.MainViewModel
import com.ozanarik.mvvmcontacts.ui.RoomLocalViewModel
import com.ozanarik.mvvmcontacts.ui.adapter.ContactsAdapter
import com.ozanarik.mvvmcontacts.util.AdapterItemClickListener
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsFragment : Fragment(),SearchView.OnQueryTextListener {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentContactsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var roomLocalViewModel: RoomLocalViewModel

    private val mainViewModel:MainViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentContactsBinding.inflate(inflater,container,false)

        (activity as AppCompatActivity).setSupportActionBar(binding.myToolbar)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

                menuInflater.inflate(R.menu.search_contact,menu)
                val searchVievItem = menu.findItem(R.id.action_Search)
                val searchActionView = searchVievItem.actionView as SearchView
                searchActionView.setOnQueryTextListener(this@ContactsFragment)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        },viewLifecycleOwner,Lifecycle.State.RESUMED)


        binding.imageView.setOnClickListener {
            signOutUser()
        }



        // Inflate the layout for this fragment
        return binding.root
    }


    private fun signOutUser(){

        mainViewModel.signOutUser()
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.signOutState.collect{signOutResult->

                when(signOutResult){

                    is Resource.Success->{
                        findNavController().navigate(R.id.action_action_Contacts_to_loginSignUpActivity)
                        roomLocalViewModel.
                    }
                    is Resource.Error->{
                        Toast.makeText(requireContext(),signOutResult.message,Toast.LENGTH_LONG).show()
                    }
                    is Resource.Loading->{
                        Toast.makeText(requireContext(),signOutResult.message,Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


    }


    private fun searchFirestoreContact(searchQuery:String){

        mainViewModel.searchFireStoreContact(searchQuery)

        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.searchState.collect{ contactList->

                when(contactList){
                    is Resource.Error->{
                        Toast.makeText(requireContext(),contactList.message,Toast.LENGTH_LONG).show()
                    }
                    is Resource.Success->{
                        contactList.let { contactsAdapter.differList.submitList(it.data!!)}
                    }
                    is Resource.Loading->{
                        Toast.makeText(requireContext(),"Getting contact data",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = Firebase.firestore
        auth = Firebase.auth
        readFireStoreContacts()
        roomLocalViewModel = ViewModelProvider(this)[RoomLocalViewModel::class.java]

        binding.floatingActionButton.setOnClickListener {

            //addContact
            AddContactDialogFragment().show((activity as AppCompatActivity).supportFragmentManager,
                AddContactDialogFragment().tag)
        }
    }

    private fun readFireStoreContacts(){

        mainViewModel.readFireStoreContactData()
        viewLifecycleOwner.lifecycleScope.launch {

            mainViewModel.readFireStoreDataState.collect{resultData->
                when(resultData){
                    is Resource.Success->{
                        resultData?.let { contactsAdapter.differList.submitList(it.data) }
                        contactsAdapter.notifyDataSetChanged()
                        Log.e("Got data","got data")
                    }
                    is Resource.Loading->{
                        Log.e("isLoading","LOADING")
                    }
                    is Resource.Error->{
                        Toast.makeText(requireContext(),resultData.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun handleRecyclerView(){
        contactsAdapter = ContactsAdapter(object : AdapterItemClickListener {
            override fun onItemClick(position: Int) {

                val currentContact = contactsAdapter.differList.currentList[position]

                val bundle = bundleOf("contact" to currentContact)

                findNavController().navigate(R.id.action_action_Contacts_to_contactDetailFragment,bundle)

            }
        })
        binding.contactRv.apply {

            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = contactsAdapter
        }
    }

    override fun onResume() {
        readFireStoreContacts()
        handleRecyclerView()
        super.onResume()
    }

    override fun onQueryTextSubmit(searchQuery: String?): Boolean {

        return false
    }

    override fun onQueryTextChange(searchQuery: String?): Boolean {
        searchQuery?.let { searchFirestoreContact(it) }
        return true
    }
}
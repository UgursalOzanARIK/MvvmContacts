package com.ozanarik.mvvmcontacts.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.FragmentUpdateContactBinding
import com.ozanarik.mvvmcontacts.ui.MainViewModel
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateContactFragment : Fragment() {
    private lateinit var binding: FragmentUpdateContactBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        firestore = Firebase.firestore
        binding = FragmentUpdateContactBinding.inflate(inflater,container,false)


        binding.buttonUpdateContact.setOnClickListener {
            updateContactDataOnFireStore()
        }


        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getContactDetail()
    }


    private fun getContactDetail() {

        val contactArgs:ContactDetailFragmentArgs by navArgs()
        val currentContact = contactArgs.contact

        Log.e("asd",currentContact.name)
        Log.e("asd",currentContact.phoneNumber)


        binding.editTextName.setText(currentContact.name)
        binding.editTextPhoneNumber.setText(currentContact.phoneNumber)

    }

    private fun getNewPerson(){

    }

    private fun updateContactDataOnFireStore(){

        val contactArgs:ContactDetailFragmentArgs by navArgs()
        val currentContact = contactArgs.contact

        mainViewModel.updateContact(currentContact,)


    }
}
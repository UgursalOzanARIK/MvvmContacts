package com.ozanarik.mvvmcontacts.ui.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.FragmentUpdateContactBinding
import com.ozanarik.mvvmcontacts.ui.MainViewModel
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class UpdateContactFragment : DialogFragment() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding:FragmentUpdateContactBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding = FragmentUpdateContactBinding.inflate(inflater,container,false)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]


        binding.buttonUpdateContact.setOnClickListener {

            val name = binding.editTextName.text.toString()
            val phoneNumber = binding.editTextPhoneNumber.text.toString()

            updateFireStoreContact(name,phoneNumber)
        }

        // Inflate the layout for this fragment
        return binding.root
    }



    private fun updateFireStoreContact(name:String,phoneNumber:String){

        val contactArgs:ContactDetailFragmentArgs by navArgs()

        val contact = contactArgs.contact

        var cname = binding.editTextName.text.toString()
        var cphoneNumber = binding.editTextPhoneNumber.text.toString()



        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.updateFireStoreContact(name,phoneNumber)

            mainViewModel.updateState.collect{updateResult->

                when(updateResult){

                    is Resource.Loading->{
                        Log.e("asd","loading")
                    }
                    is Resource.Success->{
                        Log.e("asd",updateResult.toString())

                    }
                    is Resource.Error->{
                        Log.e("asd",updateResult.message.toString())
                    }
                }
            }
        }
    }


}
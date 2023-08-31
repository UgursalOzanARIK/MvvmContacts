package com.ozanarik.mvvmcontacts.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.ozanarik.mvvmcontacts.databinding.FragmentAddContactDialogBinding
import com.ozanarik.mvvmcontacts.ui.adapter.ContactsAdapter
import com.ozanarik.mvvmcontacts.util.AdapterItemClickListener
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddContactDialogFragment : DialogFragment(){
    private lateinit var binding: FragmentAddContactDialogBinding
    private lateinit var mainViewModel: MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Inflate the layout for this fragment

        mainViewModel  = ViewModelProvider(this)[MainViewModel::class.java]

        binding = FragmentAddContactDialogBinding.inflate(inflater,container,false)




        binding.buttonSaveContact.setOnClickListener {

            saveContactToFireStore()

        }

        return binding.root
    }



    private fun saveContactToFireStore(){

        val contactName = binding.editTextNameToAdd.text.toString()
        val contactPhoneNumber = binding.editTextTextPhoneToAdd.text.toString()

        if(contactName.isNotEmpty() && contactPhoneNumber.isNotEmpty()){

            mainViewModel.uploadContactToFireStore(contactName,contactPhoneNumber)

            viewLifecycleOwner.lifecycleScope.launch {

                mainViewModel.uploadContactToFireStoreState.collect{saveResult->

                    when(saveResult){

                        is Resource.Success->{
                            Snackbar.make(binding.buttonSaveContact,"$contactName saved",Snackbar.LENGTH_LONG).show()
                        }
                        is Resource.Loading->{
                            Snackbar.make(binding.buttonSaveContact,"Saving Contact",Snackbar.LENGTH_LONG).show()
                        }
                        is Resource.Error->{
                            Snackbar.make(binding.buttonSaveContact,"${saveResult.message}",Snackbar.LENGTH_LONG).show()
                        }
                    }
                    mainViewModel.readFireStoreContactData()
                }
            }
        }
        else{

            Snackbar.make(binding.buttonSaveContact,"Contact name and contact phone number must be filled properly",Snackbar.LENGTH_LONG).show()
        }
}

}
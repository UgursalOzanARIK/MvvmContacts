package com.ozanarik.mvvmcontacts.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.FragmentContactDetailBinding
import com.ozanarik.mvvmcontacts.model.Contacts
import com.ozanarik.mvvmcontacts.ui.MainViewModel
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactDetailFragment : Fragment() {
    private lateinit var binding: FragmentContactDetailBinding
    private lateinit var contactName:String
    private lateinit var contactPhoneNumber:String

    private lateinit var mainViewModel: MainViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentContactDetailBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        //FUNCTIONS***************************************************
        updateContact()

        handleUserContactIntents()
        getIntentContactData()
        handleContactDetailImageViewButtonClicks()
        //FUNCTIONS***************************************************
    }
    private fun handleContactDetailImageViewButtonClicks(){
        binding.imgViewMore.setOnClickListener {
            handlePopUpMenuForDeleteShareContact()
        }
    }

    private fun getIntentContactData(){

        val contactArgs:ContactDetailFragmentArgs by navArgs()

        val currentContact = contactArgs.contact

        contactName = currentContact.name
        contactPhoneNumber = currentContact.phoneNumber

        binding.textViewNameDetail.text = contactName
        binding.textViewPhoneNumber.text = contactPhoneNumber
    }


    private fun handlePopUpMenuForDeleteShareContact(){

        val popupMenu = PopupMenu(requireContext(),binding.imgViewMore)

        popupMenu.menuInflater.inflate(R.menu.popup,popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item->

            when(item.itemId){
                R.id.action_Delete->{
                    Log.e("contactDeleted","deleted $contactName")

                    deleteContactAlertDialog()

                    true
                }
                R.id.action_Share->{
                    shareContact()
                    true
                }else->
            {
                false
            }

            }
        }
        popupMenu.show()
    }
    private fun deleteContactAlertDialog(){

        val ad = AlertDialog.Builder(requireContext()).apply {

            setMessage("Are you sure you want to delete $contactName ?")
            setTitle("Delete Contact")
            setIcon(R.drawable.baseline_delete_24)

            setNegativeButton("No"){dialogInterface,i->

                Toast.makeText(requireContext(),"Dismissed", Toast.LENGTH_LONG).show()
            }
            setPositiveButton("Yes"){dialogInterface,i->
                deleteContactFromFireStore()
            }
            create().show()
        }
    }



    private fun updateContact(){

        binding.buttonUpdate.setOnClickListener {

            val contact = Contacts(0,contactName,contactPhoneNumber)

            val bundle = bundleOf("contact" to contact)

            findNavController().navigate(R.id.action_contactDetailFragment_to_updateContactFragment,bundle)
        }
    }


    private fun deleteContactFromFireStore(){
        mainViewModel.deleteFireStoreContact(contactName,contactPhoneNumber)

        viewLifecycleOwner.lifecycleScope.launch {

            mainViewModel.deleteFromFireStoreStateFlow.collect{hasDeleted->
                when(hasDeleted){
                    is Resource.Success->{
                        Snackbar.make(binding.imgViewMore,"$contactName deleted from database!",
                            Snackbar.LENGTH_LONG).show()
                       findNavController().navigate(R.id.action_contactDetailFragment_to_action_Contacts)

                    }
                    is Resource.Loading->{
                        Snackbar.make(binding.imgViewMore,"Deleting $contactName, please wait...",
                            Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Error->{
                        Snackbar.make(binding.imgViewMore,hasDeleted.message!!, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun shareContact(){

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"

        intent.putExtra(Intent.EXTRA_TEXT,contactPhoneNumber)

        val intentChooser = Intent.createChooser(intent,"Share Contact")

        startActivity(intentChooser)
    }

    private fun handleUserContactIntents(){

        binding.cardViewWhatsapp.setOnClickListener {
            whatsappSendIntent()
        }

        binding.cardViewCall.setOnClickListener {

            callIntent()
        }
        binding.cardViewMessage.setOnClickListener {
            messageIntent()
        }
    }
    private fun callIntent(){
        val uriParseTel = Uri.parse("tel:${contactPhoneNumber}")
        val intent = Intent(Intent.ACTION_DIAL,uriParseTel)

        startActivity(intent)
    }
    private fun messageIntent(){

        val uriParseTel = Uri.parse("smsto:${contactPhoneNumber}")
        val intent = Intent(Intent.ACTION_SENDTO,uriParseTel)

        startActivity(intent)
    }

    private fun whatsappSendIntent(){
        try {
            val uriParseWhatsapp = Uri.parse("https://api.whatsapp.com/send?phone=$contactPhoneNumber")

            val intent = Intent(Intent.ACTION_VIEW,uriParseWhatsapp)

            startActivity(intent)

        }catch (e:Exception){
            Snackbar.make(binding.cardViewWhatsapp,e.localizedMessage!!,Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.imageViewContactPhoto.setImageBitmap(null)
    }
}
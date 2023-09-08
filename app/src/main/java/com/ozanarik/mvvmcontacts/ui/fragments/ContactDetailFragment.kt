package com.ozanarik.mvvmcontacts.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.FragmentContactDetailBinding
import com.ozanarik.mvvmcontacts.databinding.FragmentUpdateContactBinding
import com.ozanarik.mvvmcontacts.model.Contacts
import com.ozanarik.mvvmcontacts.ui.MainViewModel
import com.ozanarik.mvvmcontacts.ui.RoomLocalViewModel
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactDetailFragment : Fragment() {
    private lateinit var binding: FragmentContactDetailBinding
    private lateinit var contactName:String
    private lateinit var contactPhoneNumber:String

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var localViewModel: RoomLocalViewModel
    private lateinit var mainViewModel: MainViewModel

    private lateinit var animJob: Job

    private var selectedImg: Uri?=null

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

        localViewModel = ViewModelProvider(this)[RoomLocalViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        animJob = Job()

        //FUNCTIONS***************************************************
        handleUserContactIntents()
        getIntentContactData()
        handleActivityResultLaunchers()
        handleContactDetailImageViewButtonClicks()
        addToFavourites()
        updateContact()
        //FUNCTIONS***************************************************
    }
    private fun handleContactDetailImageViewButtonClicks(){
        binding.imgViewMore.setOnClickListener {
            handlePopUpMenuForDeleteShareContact()
        }

        binding.cardViewContact.setOnClickListener {
            pickImageForContact()
        }
    }
    private fun getIntentContactData(){

        val contactArgs:ContactDetailFragmentArgs by navArgs()

        val currentContact = contactArgs.contact

        contactName = currentContact.name.toString()
        contactPhoneNumber = currentContact.phoneNumber.toString()

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
                }else->{
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

    private fun addToFavourites(){

        binding.imageViewFav.setOnClickListener {

            val newContact = Contacts(0,contactName,contactPhoneNumber)

            localViewModel.insertContact(newContact)
            Snackbar.make(binding.imageViewFav,"$contactName added to favourites!",Snackbar.LENGTH_LONG).show()
        }
    }

    private fun updateContact(){

        binding.buttonUpdate.setOnClickListener {

            UpdateContactFragment().show((activity as AppCompatActivity).supportFragmentManager,UpdateContactFragment().tag)

            val bundle = Bundle().apply {
                val contactBundle = Contacts(0,contactName,contactPhoneNumber)
                putSerializable("contact",contactBundle)
            }

            findNavController().navigate(R.id.action_contactDetailFragment_to_updateContactFragment)

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
                    }
                    is Resource.Loading->{
                        Snackbar.make(binding.imgViewMore,"Deleting $contactName, please wait...",
                            Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Error->{
                        Snackbar.make(binding.imgViewMore,hasDeleted.message!!, Snackbar.LENGTH_LONG).show()
                    }
                }
                delay(2000L)
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

    private fun pickImageForContact(){

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(binding.cardViewContact,"Permission needed to pick an image for your contact",Snackbar.LENGTH_INDEFINITE).apply {
                    setAction("Grant Permission"){
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }.show()
            }else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }else{
            imagePickerLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))

        }
    }
    private fun handleActivityResultLaunchers(){
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

            if(activityResult.resultCode == AppCompatActivity.RESULT_OK){

                val intentData = activityResult.data

                if(intentData!=null){
                    selectedImg = intentData.data
                    selectedImg?.let { binding.imageViewContactPhoto.setImageURI(it) }
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted->

            when(isGranted){
                true->{
                    imagePickerLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
                }
                false->{
                    Snackbar.make(binding.cardViewContact,"Please grant permission to be able to pick a photo for your contact",Snackbar.LENGTH_LONG).show()
                }
            }
        }
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
        animJob.cancel()
    }
}
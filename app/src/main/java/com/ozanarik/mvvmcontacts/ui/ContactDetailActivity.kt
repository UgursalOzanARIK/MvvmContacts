package com.ozanarik.mvvmcontacts.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.ActivityContactDetailBinding
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactDetailBinding
    private lateinit var contactName:String
    private lateinit var contactPhoneNumber:String

    private lateinit var permissionLauncher:ActivityResultLauncher<String>
    private lateinit var imagePickerLauncher:ActivityResultLauncher<Intent>

    private lateinit var mainViewModel: MainViewModel

    var selectedImg: Uri ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityContactDetailBinding.inflate(layoutInflater)

        handleActivityResultLaunchers()


        contactName = intent.getStringExtra("contactName")!!
        contactPhoneNumber = intent.getStringExtra("contactPhoneNumber")!!



        handleUserContactIntents()
        handleContactInfo()


        binding.cardViewContact.setOnClickListener {
            pickImageForContact()

        }


        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val root = binding.root
        setContentView(root)


    }



    private fun pickImageForContact(){

        if (ContextCompat.checkSelfPermission(this@ContactDetailActivity,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this@ContactDetailActivity,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(binding.cardViewContact,"Permission needed to pick an image for your contact",Snackbar.LENGTH_INDEFINITE).apply {
                    setAction("Grant Permission"){
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }.show()
            }else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }else{
            imagePickerLauncher.launch(Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI))

        }
    }


    private fun handleActivityResultLaunchers(){


        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){activityResult->

            if(activityResult.resultCode == RESULT_OK){

                val intentData = activityResult.data

                if(intentData!=null){

                    selectedImg = intentData.data

                    selectedImg?.let { binding.imageViewContactPhoto.setImageURI(it) }
                    mainViewModel.uploadPhotoToFirebaseStorage(selectedImg!!)

                    lifecycleScope.launch {
                        mainViewModel.uploadPhotoState.collect{

                            when(it){
                                is Resource.Success->{
                                    Toast.makeText(this@ContactDetailActivity,"Image successfully updated!",Toast.LENGTH_LONG).show()
                                }
                                is Resource.Error->{
                                    Toast.makeText(this@ContactDetailActivity,it.message,Toast.LENGTH_LONG).show()
                                }
                                is Resource.Loading->{
                                    Toast.makeText(this@ContactDetailActivity,"Updating photo...",Toast.LENGTH_LONG).show()
                                }
                            }

                        }
                    }

                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->

            when(isGranted){
                true->{
                    imagePickerLauncher.launch(Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI))

                }
                false->{
                    Snackbar.make(binding.cardViewContact,"Please grant permission to be able to pick a photo for your contact",Snackbar.LENGTH_LONG).show()
                }
            }

        }
    }


    private fun handleContactInfo(){
        binding.textViewNameDetail.text = contactName
        binding.textViewPhoneNumber.text = contactPhoneNumber
    }

    private fun handleUserContactIntents(){

        binding.cardViewWhatsapp.setOnClickListener {

            try {
                val uriParseTel = Uri.parse("https://api.whatsapp.com/send?phone=$contactPhoneNumber")

                val intent = Intent(Intent.ACTION_VIEW,uriParseTel)

                startActivity(intent)

            }catch (e:Exception){
                Snackbar.make(binding.cardViewWhatsapp,e.localizedMessage!!,Snackbar.LENGTH_LONG).show()
            }

        }

        binding.cardViewCall.setOnClickListener {

            val uriParseTel = Uri.parse("tel:${contactPhoneNumber}")
            val intent = Intent(Intent.ACTION_DIAL,uriParseTel)

            startActivity(intent)

        }

        binding.cardViewMessage.setOnClickListener {

            val uriParseTel = Uri.parse("smsto:${contactPhoneNumber}")
            val intent = Intent(Intent.ACTION_SENDTO,uriParseTel)

            startActivity(intent)
        }


    }



}
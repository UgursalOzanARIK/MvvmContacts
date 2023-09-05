package com.ozanarik.mvvmcontacts.ui

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.ActivityContactDetailBinding
import com.ozanarik.mvvmcontacts.model.Contacts
import com.ozanarik.mvvmcontacts.util.DatastoreManager
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContactDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactDetailBinding
    private lateinit var contactName:String
    private lateinit var contactPhoneNumber:String

    private lateinit var permissionLauncher:ActivityResultLauncher<String>
    private lateinit var imagePickerLauncher:ActivityResultLauncher<Intent>

    private lateinit var mainViewModel: MainViewModel
    private lateinit var localViewModel: RoomLocalViewModel

    private lateinit var animJob:Job

    var selectedImg: Uri ?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactDetailBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        animJob = Job()


        //VIEWMODELS******************************************************************
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        localViewModel = ViewModelProvider(this)[RoomLocalViewModel::class.java]
        //VIEWMODELS******************************************************************


        //FUNCTIONS***************************************************
        handleUserContactIntents()
        getIntentContactData()
        handleContactInfo()
        handleActivityResultLaunchers()
        getFavContact()
        handleContactDetailImageViewButtonClicks()
        //FUNCTIONS***************************************************


    }

    private fun handleContactDetailImageViewButtonClicks(){
        binding.imgViewMore.setOnClickListener {
            handlePopUpMenuForDeleteShareContact()
        }

        binding.cardViewContact.setOnClickListener {
            pickImageForContact()
        }

        binding.textViewAddToFav.setOnClickListener {

            addToFavorites()
        }
    }

    private fun getIntentContactData(){

        contactName = intent.getStringExtra("contactName")!!
        contactPhoneNumber = intent.getStringExtra("contactPhoneNumber")!!
    }


    private fun getFavContact(){
        lifecycleScope.launch {

            localViewModel.getFavContactBool().observe(this@ContactDetailActivity, Observer {isFavContact->

                when(isFavContact){
                    true->{
                        Log.e("asd","viewmodel get favcontact function değeri : $isFavContact")
                        handleFavAnimation(true)
                        binding.textViewAddToFav.text = "Delete From Favourites"
                    }
                    false->{
                        Log.e("asd","viewmodel get favcontact function değeri : $isFavContact")
                        handleFavAnimation(false)
                        binding.textViewAddToFav.text = "Add To Favourites"
                    }
                }
            })
        }
    }
    private fun handleFavAnimation(isFav:Boolean){

        val scaleXValue = "scaleX"
        val scaleYValue = "scaleY"
        val targetObj = binding.imageViewFav

        val scaleX = ObjectAnimator.ofFloat(targetObj,scaleXValue,1.0f,1.5f).apply {
            repeatCount = 1
            repeatMode = ObjectAnimator.REVERSE
        }
        val scaleY = ObjectAnimator.ofFloat(targetObj,scaleYValue,1.0f,1.5f).apply {
            repeatCount = 1
            repeatMode = ObjectAnimator.REVERSE
        }

        val multiAnim = AnimatorSet().apply {
            duration = 200L
            playTogether(scaleY,scaleX)
        }.start()


        if(isFav){
            targetObj.setColorFilter(Color.RED)

        }else{
            targetObj.setColorFilter(Color.WHITE)
        }
    }

    private fun addToFavorites(){

        val newContact = Contacts(0,contactName,contactPhoneNumber)

        localViewModel.insertContact(newContact)

        lifecycleScope.launch {
            localViewModel.isFav.collect{isFavContact->
                when(isFavContact){
                    true->{
                        localViewModel.saveFavContactBool(true)
                        handleFavAnimation(true)
                        Log.e("asd","${isFavContact.toString()} : isfavcontact viewmodelda ki stateflow")
                        Log.e("asd","contact detail datastoredan çekilen bool : true")
                    }
                    false->{
                        localViewModel.saveFavContactBool(false)
                        handleFavAnimation(false)
                        Log.e("asd","{isFavContact.toString()} : isfavcontact viewmodelda ki stateflow")
                        Log.e("asd","contact detail datastoredan çekilen bool : false")
                    }
                }
            }
        }
    }

   private fun handlePopUpMenuForDeleteShareContact(){

       val popupMenu = PopupMenu(this@ContactDetailActivity,binding.imgViewMore)

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

        val ad = AlertDialog.Builder(this).apply {

            setMessage("Are you sure you want to delete $contactName ?")
            setTitle("Delete Contact")
            setIcon(R.drawable.baseline_delete_24)

            setNegativeButton("No"){dialogInterface,i->

                Toast.makeText(this@ContactDetailActivity,"Dismissed",Toast.LENGTH_LONG).show()
            }
            setPositiveButton("Yes"){dialogInterface,i->
                deleteContactFromFireStore()
                finish()
            }
            create().show()
        }
    }

    private fun deleteContactFromFireStore(){
        mainViewModel.deleteFireStoreContact(contactName,contactPhoneNumber)
        lifecycleScope.launch {

            mainViewModel.deleteFromFireStoreStateFlow.collect{hasDeleted->
                when(hasDeleted){
                    is Resource.Success->{
                        Snackbar.make(binding.imgViewMore,"$contactName deleted from database!",Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Loading->{
                        Snackbar.make(binding.imgViewMore,"Deleting $contactName, please wait...",Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Error->{
                        Snackbar.make(binding.imgViewMore,hasDeleted.message!!,Snackbar.LENGTH_LONG).show()
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

        val intentChooser = Intent.createChooser(intent,"Share Contact Information")

        startActivity(intentChooser)
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

    override fun onResume() {
        super.onResume()
        handleFavAnimation(localViewModel.isFav.value)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.imageViewContactPhoto.setImageBitmap(null)
        binding.textViewAddToFav.setOnClickListener (null)
        animJob.cancel()
    }
}
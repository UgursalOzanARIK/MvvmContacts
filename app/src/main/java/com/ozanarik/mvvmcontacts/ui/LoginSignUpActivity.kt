package com.ozanarik.mvvmcontacts.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.ActivityLoginSignUpBinding
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.sign

@AndroidEntryPoint
class LoginSignUpActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginSignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginSignUpBinding.inflate(layoutInflater)

        val root = binding.root
        setContentView(root)
        auth = Firebase.auth

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]


        if(auth.currentUser!=null){
            startActivity(Intent(this@LoginSignUpActivity,MainActivity::class.java))
            finish()
        }

        binding.buttonSignUp.setOnClickListener {

            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            signUp(email,password)
            startActivity(Intent(this@LoginSignUpActivity,MainActivity::class.java))
            finish()

            }
        }



    fun signUp(email:String,password:String){

        mainViewModel.signUp(email,password)

        lifecycleScope.launch {

            mainViewModel.signUpResult.collect{result->

                when(result){
                    is Resource.Success->{
                        //RECYCLERVIEW TO BE UPDATED
                        Snackbar.make(binding.buttonSignUp,"helal lan",Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Error->{
                        Snackbar.make(binding.buttonSignUp,result.message.toString(),Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Loading->{
                        Snackbar.make(binding.buttonSignUp,"Signing you up",Snackbar.LENGTH_LONG).show()
                    }
                }
            }

        }


    }
    }
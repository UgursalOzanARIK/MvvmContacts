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
import com.ozanarik.mvvmcontacts.databinding.ActivityLoginSignUpBinding
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

        handleCurrentUser()

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        binding.buttonSignUp.setOnClickListener {

            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            signUp(email,password)
            }

        binding.buttonSignIn.setOnClickListener {

            signIn()
        }

        }

    private fun signIn(){

        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()
            if(email.isNotEmpty() && password.isNotEmpty()){
                mainViewModel.signIn(email,password)

                lifecycleScope.launch {
                    mainViewModel.signInState.collect{signInState->
                        when(signInState){
                            is Resource.Success->{
                                startActivity(Intent(this@LoginSignUpActivity,MainActivity::class.java))
                                finish()
                            }
                            is Resource.Error->{
                                Snackbar.make(binding.buttonSignIn,signInState.message!!,Snackbar.LENGTH_LONG).show()
                            }
                            is Resource.Loading->{
                                Snackbar.make(binding.buttonSignIn,"Loading",Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }

            }
    }

    private fun signUp(email:String, password:String){

        mainViewModel.signUp(email,password)

        lifecycleScope.launch {

            mainViewModel.signUpResult.collect{result->

                when(result){
                    is Resource.Success->{
                        Snackbar.make(binding.buttonSignUp,"Signed up successfully!",Snackbar.LENGTH_LONG).show()
                        startActivity(Intent(this@LoginSignUpActivity,MainActivity::class.java))
                        finish()
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

    private fun handleCurrentUser(){
        if(auth.currentUser!=null){
            startActivity(Intent(this@LoginSignUpActivity,MainActivity::class.java))
            finish()
        }

    }

    }

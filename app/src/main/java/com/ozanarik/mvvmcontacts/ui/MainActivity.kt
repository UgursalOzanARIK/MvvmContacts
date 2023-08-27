package com.ozanarik.mvvmcontacts.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)




        binding.floatingActionButton.setOnClickListener {


            //addContact
            AddContactDialogFragment().show(supportFragmentManager,AddContactDialogFragment().tag)

        }


    }
}
package com.ozanarik.mvvmcontacts.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.FragmentFavouriteContactsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavouriteContactsFragment : Fragment() {
    private lateinit var binding:FragmentFavouriteContactsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFavouriteContactsBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

}
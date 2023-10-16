package com.ozanarik.mvvmcontacts.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ozanarik.mvvmcontacts.R
import com.ozanarik.mvvmcontacts.databinding.FragmentFavouriteContactsBinding
import com.ozanarik.mvvmcontacts.ui.RoomLocalViewModel
import com.ozanarik.mvvmcontacts.ui.adapter.ContactsAdapter
import com.ozanarik.mvvmcontacts.util.AdapterItemClickListener
import com.ozanarik.mvvmcontacts.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavouriteContactsFragment : Fragment() {
    private lateinit var binding:FragmentFavouriteContactsBinding
    private lateinit var roomLocalViewModel: RoomLocalViewModel

    private lateinit var contactsAdapter: ContactsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFavouriteContactsBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUpRecyclerView()
        roomLocalViewModel = ViewModelProvider(this)[RoomLocalViewModel::class.java]

        getFavouriteContactList()

    }

    private fun getFavouriteContactList(){

        roomLocalViewModel.getAllContacts()

        viewLifecycleOwner.lifecycleScope.launch {
            roomLocalViewModel.localContactsData.collect{contactList->

                when(contactList){
                    is Resource.Success->{
                        contactList.data?.let { contactsAdapter.differList.submitList(it) }
                    }
                    is Resource.Error->{
                        Toast.makeText(requireContext(),contactList.message,Toast.LENGTH_LONG).show()
                    }
                    is Resource.Loading->{
                        Toast.makeText(requireContext(),"Fetching contacts...",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


    }

    private fun setUpRecyclerView() {

        contactsAdapter = ContactsAdapter(object : AdapterItemClickListener {
            override fun onItemClick(position: Int) {

                val currentContact = contactsAdapter.differList.currentList[position]

                val bundle = bundleOf("contact" to currentContact)

                findNavController().navigate(R.id.action_action_FavContacts_to_contactDetailFragment,bundle)

                Log.e("asd",currentContact.name)

            }
        })

        binding.favContactsRv.apply {

            layoutManager = LinearLayoutManager(requireContext())
            adapter = contactsAdapter
            setHasFixedSize(true)

        }


    }


}
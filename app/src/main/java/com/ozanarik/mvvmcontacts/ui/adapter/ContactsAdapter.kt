package com.ozanarik.mvvmcontacts.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ozanarik.mvvmcontacts.databinding.ItemContactListBinding
import com.ozanarik.mvvmcontacts.model.Contacts

class ContactsAdapter: RecyclerView.Adapter<ContactsAdapter.ContactsHolder>() {



    inner class ContactsHolder(val binding:ItemContactListBinding):RecyclerView.ViewHolder(binding.root)

    private val diffUtil = object: DiffUtil.ItemCallback<Contacts>(){

        override fun areItemsTheSame(oldItem: Contacts, newItem: Contacts): Boolean {

            return oldItem.phoneNumber == newItem.phoneNumber

        }

        override fun areContentsTheSame(oldItem: Contacts, newItem: Contacts): Boolean {

            return oldItem==newItem
        }
    }
    val differList = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsHolder {

        val layoutFrom = LayoutInflater.from(parent.context)
        val binding:ItemContactListBinding = ItemContactListBinding.inflate(layoutFrom,parent,false)
        return ContactsHolder(binding)

    }

    override fun onBindViewHolder(holder: ContactsHolder, position: Int) {
        val currentContact = differList.currentList[position]

        holder.binding.apply {
            textViewName.text = currentContact.name
            textViewPhone.text = currentContact.phoneNumber
        }

    }

    override fun getItemCount(): Int {
        return differList.currentList.size
    }
}
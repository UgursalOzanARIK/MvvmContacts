package com.ozanarik.mvvmcontacts.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ozanarik.mvvmcontacts.databinding.ItemContactListBinding
import com.ozanarik.mvvmcontacts.model.Contacts
import com.ozanarik.mvvmcontacts.util.AdapterItemClickListener


class ContactsAdapter(private val clickListener: AdapterItemClickListener): RecyclerView.Adapter<ContactsAdapter.ContactsHolder>(){

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
        }

        holder.itemView.apply {
            setOnClickListener {
                clickListener.onItemClick(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return differList.currentList.size
    }

}

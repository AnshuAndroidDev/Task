package com.example.interviewtask.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.interviewtask.databinding.ItemUserLayoutBinding
import com.example.interviewtask.model.UserResponse

class UserAdapter(private var userList: List<UserResponse.UserData>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // ViewHolder now uses ViewBinding
    class UserViewHolder(val binding: ItemUserLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.txtName.text = "${user.firstName} ${user.lastName}"
        holder.binding.txtEmail.text = user.email

        // Load the avatar image using Glide
        Glide.with(holder.binding.imgAvatar.context)
            .load(user.avatar)
            .into(holder.binding.imgAvatar)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateData(newUserList: List<UserResponse.UserData>) {
        userList = newUserList
        notifyDataSetChanged()
    }
}

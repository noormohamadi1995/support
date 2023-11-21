package com.example.support.Panel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.support.databinding.RecyclerviewDrawIteBinding
import com.example.support.new_model.UserWithDraw

class DrawListAdapter : ListAdapter<UserWithDraw, DrawListAdapter.DrawItemViewHolder>(
    object : DiffUtil.ItemCallback<UserWithDraw>() {
        override fun areItemsTheSame(oldItem: UserWithDraw, newItem: UserWithDraw) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: UserWithDraw, newItem: UserWithDraw) =
            oldItem == newItem

    }
) {

    inner class DrawItemViewHolder(private val binding: RecyclerviewDrawIteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userWithDraw: UserWithDraw) {
            binding.tvAmount.text = userWithDraw.amount?.plus(" ")
            binding.tvCardNumber.text = userWithDraw.cardNumber
            binding.tvCardName.text = userWithDraw.owener
            binding.tvStatus.text = when(userWithDraw.status){
                "done" -> "انجام شد"
                "pending" -> "درانتظار پاسخ ادمین"
                "fail" -> "رد شد"
                else -> ""
            }
            binding.tvDescription.text = userWithDraw.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DrawItemViewHolder(
        RecyclerviewDrawIteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )


    override fun onBindViewHolder(holder: DrawItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
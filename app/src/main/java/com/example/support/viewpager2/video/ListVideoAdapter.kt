package com.example.support.viewpager2.video

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.support.R
import com.example.support.databinding.RecyclerviewItemListVideoBinding


class ListVideoAdapter(
    private val onClickItem : (position : Int) -> Unit
) : ListAdapter<VideoListItem,ListVideoAdapter.VideoListItemViewHolder>(
    object : DiffUtil.ItemCallback<VideoListItem>(){
        override fun areItemsTheSame(oldItem: VideoListItem, newItem: VideoListItem) = oldItem == newItem

        override fun areContentsTheSame(oldItem: VideoListItem, newItem: VideoListItem) = oldItem == newItem

    }
) {

    inner class VideoListItemViewHolder (val binding : RecyclerviewItemListVideoBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item : VideoListItem){
            Glide.with(itemView.context)
                .load(item.image)
                .placeholder(R.drawable.ima)
                .error(R.drawable.ima)
                .into(binding.image)

            binding.root.setOnClickListener {
                onClickItem.invoke(adapterPosition)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =  VideoListItemViewHolder (
        RecyclerviewItemListVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: VideoListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
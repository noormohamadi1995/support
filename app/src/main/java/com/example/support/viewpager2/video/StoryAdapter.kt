package com.example.support.viewpager2.video

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.support.databinding.RecyclerviewStoryItemBinding

class StoryAdapter(
    private val onClick : (item : VideoListItem) -> Unit
) : ListAdapter<VideoListItem,StoryAdapter.StoryViewHolder>(
    object : DiffUtil.ItemCallback<VideoListItem>(){
        override fun areItemsTheSame(oldItem: VideoListItem, newItem: VideoListItem) = oldItem == newItem

        override fun areContentsTheSame(oldItem: VideoListItem, newItem: VideoListItem) = oldItem == newItem
    }
) {

    inner class StoryViewHolder(val binding : RecyclerviewStoryItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item : VideoListItem){
            binding.txtTitle.text = item.title
            Glide.with(binding.root.context)
                .load(item.image)
                .circleCrop()
                .into(binding.storyImg)
            binding.root.setOnClickListener {
                onClick.invoke(item)
            }
            binding.executePendingBindings()
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StoryViewHolder(
        RecyclerviewStoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
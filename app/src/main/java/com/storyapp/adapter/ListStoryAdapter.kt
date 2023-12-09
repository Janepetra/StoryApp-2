package com.storyapp.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.storyapp.R
import com.storyapp.customview.Helper
import com.storyapp.dashboard.DetailStoryActivity
import com.storyapp.databinding.ItemRowBinding
import com.storyapp.db.local.entity.Story
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListStoryAdapter: PagingDataAdapter<Story, ListStoryAdapter.MyViewHolder>(DiffCallback) {
    private var story = mutableListOf<Story>()

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: Story)
    }

    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int ): MyViewHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = story.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
        holder.itemView.setOnClickListener {
            getItem(position)?.let { it1 -> onItemClickCallback.onItemClicked(it1) }
        }
    }

    class MyViewHolder(private val binding: ItemRowBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(storyList: Story) {
            with(binding) {
                Glide.with(this.root.context)
                    .load(storyList.photoUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(binding.ivProfile)
                tvName.text = storyList.name
                upTime.text = Helper.formatDateToString(storyList.createdAt.toString())
            }
        }
    }
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }
        }
    }
}
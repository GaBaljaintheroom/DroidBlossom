package com.droidblossom.archive.presentation.ui.mypage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.droidblossom.archive.databinding.ItemMyCapsuleBinding
import com.droidblossom.archive.domain.model.common.MyCapsule

class MyCapsuleRVA( val MyCapsuleFlow: (MyCapsule) -> Unit) :
    ListAdapter<MyCapsule, MyCapsuleRVA.ItemViewHolder>(differ) {

    inner class ItemViewHolder(
        private val binding: ItemMyCapsuleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MyCapsule) {
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder(
            ItemMyCapsuleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<MyCapsule>() {
            override fun areItemsTheSame(oldItem: MyCapsule, newItem: MyCapsule): Boolean {
                return oldItem.capsuleId == newItem.capsuleId
            }

            override fun areContentsTheSame(oldItem: MyCapsule, newItem: MyCapsule): Boolean {
                return oldItem == newItem
            }
        }
    }
}
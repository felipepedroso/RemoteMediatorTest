package br.pedroso.remotemediatortest.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import br.pedroso.remotemediatortest.entity.Item

class ItemsAdapter(
    private val removeItemCallback: (Item) -> Unit
) : PagingDataAdapter<Item, ItemViewHolder>(DIFF_CALLBACK) {
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        getItem(position)?.let { item -> holder.bind(item) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder.create(parent, removeItemCallback)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {
            override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
            override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem.id == newItem.id
        }
    }
}

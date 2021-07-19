package br.pedroso.remotemediatortest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.pedroso.remotemediatortest.databinding.ItemBinding
import br.pedroso.remotemediatortest.entity.Item

class ItemViewHolder(
    private val binding: ItemBinding,
    private val removeItemCallback: (Item) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Item) {
        binding.descriptionTextView.text = item.description

        binding.removeItemButton.setOnClickListener {
            removeItemCallback(item)
        }
    }

    companion object {
        fun create(parent: ViewGroup, removeItemCallback: (Item) -> Unit): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(binding, removeItemCallback)
        }
    }
}

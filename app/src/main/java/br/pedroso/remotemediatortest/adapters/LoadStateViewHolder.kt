package br.pedroso.remotemediatortest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import br.pedroso.remotemediatortest.databinding.ItemLoadStateBinding

class LoadStateViewHolder(
    private val binding: ItemLoadStateBinding,
    private val retryCallback: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(loadState: LoadState) = with(binding) {
        loadingGroup.isVisible = loadState is LoadState.Loading
        errorGroup.isVisible = loadState is LoadState.Error

        if (loadState is LoadState.Error) {
            retryButton.setOnClickListener { retryCallback.invoke() }
        } else {
            retryButton.setOnClickListener(null)
        }
    }

    companion object {
        fun create(parent: ViewGroup, retryCallback: () -> Unit): LoadStateViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemLoadStateBinding.inflate(layoutInflater, parent, false)
            return LoadStateViewHolder(binding, retryCallback)
        }
    }
}

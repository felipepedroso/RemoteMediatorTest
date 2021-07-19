package br.pedroso.remotemediatortest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.pedroso.remotemediatortest.api.ItemsApi
import br.pedroso.remotemediatortest.entity.Item
import br.pedroso.remotemediatortest.paging.PagesCache
import br.pedroso.remotemediatortest.repository.ItemsPageFetcher
import br.pedroso.remotemediatortest.repository.ItemsRepository

@Suppress("UNCHECKED_CAST")
class MainActivityViewModelFactory : ViewModelProvider.Factory {
    private val pagesCache = PagesCache(Item::id, 1)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            MainActivityViewModel::class.java -> MainActivityViewModel(
                itemsRepository = ItemsRepository(
                    itemsPageFetcher = ItemsPageFetcher(ItemsApi()),
                    pagedCache = pagesCache
                ),
                pagesCache = pagesCache
            )
            else -> throw IllegalStateException("Invalid view model type.")
        } as T
    }
}

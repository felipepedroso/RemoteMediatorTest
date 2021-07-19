package br.pedroso.remotemediatortest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import br.pedroso.remotemediatortest.entity.Item
import br.pedroso.remotemediatortest.paging.PagesCache
import br.pedroso.remotemediatortest.repository.ItemsRepository

class MainActivityViewModel(
    itemsRepository: ItemsRepository,
    private val pagesCache: PagesCache<Int, Item, Int>
) : ViewModel() {

    val pagingDataFlow = itemsRepository.itemPager.flow.cachedIn(viewModelScope)

    fun removeItem(item: Item) {
        pagesCache.Transaction {
            delete(item)
        }
    }
}

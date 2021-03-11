package br.pedroso.remotemediatortest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import br.pedroso.remotemediatortest.repository.ItemsRepository

class MainActivityViewModel(
    itemsRepository: ItemsRepository
) : ViewModel() {

    val pagingDataFlow = itemsRepository.itemPager.flow.cachedIn(viewModelScope)
}

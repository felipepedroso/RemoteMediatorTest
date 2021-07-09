package br.pedroso.remotemediatortest.repository

import br.pedroso.remotemediatortest.api.ItemsApi
import br.pedroso.remotemediatortest.entity.Item
import br.pedroso.remotemediatortest.paging.Page
import br.pedroso.remotemediatortest.paging.PageFetcher
import br.pedroso.remotemediatortest.paging.PageKey

class ItemsPageFetcher(
    private val itemsApi: ItemsApi,
) : PageFetcher<Int, Item> {
    override suspend fun fetchPage(pageNumber: Int): Page<Int, Item> {
        val itemsPage = itemsApi.getItemsPage(pageNumber)

        return Page(
            key = PageKey(itemsPage.currentPage, itemsPage.previousPage, itemsPage.nextPage),
            items = itemsPage.items
        )
    }
}
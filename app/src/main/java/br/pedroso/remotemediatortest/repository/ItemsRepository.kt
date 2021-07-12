package br.pedroso.remotemediatortest.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import br.pedroso.remotemediatortest.entity.Item
import br.pedroso.remotemediatortest.paging.PagesCache
import br.pedroso.remotemediatortest.paging.PagesCacheRemoteMediator

class ItemsRepository(
    itemsPageFetcher: ItemsPageFetcher,
    private val pagedCache: PagesCache<Int, Item, Int>
) {

    @OptIn(ExperimentalPagingApi::class)
    val itemPager = Pager(
        config = PagingConfig(pageSize = 20),
        initialKey = STARTING_PAGE,
        remoteMediator = PagesCacheRemoteMediator(
            pagesCache = pagedCache,
            startingPageKey = STARTING_PAGE,
            pageFetcher = itemsPageFetcher,
            itemIdGetter = Item::id
        ),
        pagingSourceFactory = pagedCache.pagingSourceFactory
    )

    companion object {
        private const val STARTING_PAGE = 1
    }
}

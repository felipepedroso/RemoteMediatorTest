package br.pedroso.remotemediatortest.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import br.pedroso.remotemediatortest.api.ItemsApi
import br.pedroso.remotemediatortest.entity.Item
import br.pedroso.remotemediatortest.paging.CachePagingSource
import br.pedroso.remotemediatortest.paging.CachedRemoteMediator
import br.pedroso.remotemediatortest.paging.PagedCache

class ItemsRepository(
    private val itemsApi: ItemsApi,
    private val pagedCache: PagedCache<Int, Item, Int>
) {

    @OptIn(ExperimentalPagingApi::class)
    val itemPager = Pager(
        config = PagingConfig(pageSize = 10),
        initialKey = STARTING_PAGE,
        remoteMediator = CachedRemoteMediator(
            pagedCache = pagedCache,
            startingPageKey = 1,
            fetchPage = itemsApi::getItemsPage
        ),
        pagingSourceFactory = { CachePagingSource(pagedCache.getSnapshot(), STARTING_PAGE) }
    )

    companion object {
        private const val STARTING_PAGE = 1
    }
}

package br.pedroso.remotemediatortest.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

class PagesCachePagingSource<PageKeyType : Any, ItemType : Any>(
    private val cache: Map<PageKeyType, List<ItemType>>,
    private val pageKeysIndex: HashMap<PageKeyType, PageKey<PageKeyType>>,
    private val startingPageKey: PageKeyType,
) : PagingSource<PageKeyType, ItemType>() {

    override fun getRefreshKey(state: PagingState<PageKeyType, ItemType>): PageKeyType? =
        startingPageKey

    override suspend fun load(params: LoadParams<PageKeyType>): LoadResult<PageKeyType, ItemType> {
        return try {
            val pageKey = pageKeysIndex.getValue(params.key ?: startingPageKey)
            val items = cache.getValue(pageKey.value)

            LoadResult.Page(
                data = items,
                if (items.isEmpty() && params is LoadParams.Prepend) null else pageKey.previousPageKey,
                if (items.isEmpty() && params is LoadParams.Append) null else pageKey.nextPageKey
            )
        } catch (error: Throwable) {
            LoadResult.Error(error)
        }
    }
}

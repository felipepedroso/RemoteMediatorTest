package br.pedroso.remotemediatortest.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

class PagesCachePagingSource<PageKeyType : Any, ItemType : Any>(
    private val dataSnapshot: PagesCacheSnapshot<PageKeyType, ItemType>,
    private val startingPageKey: PageKeyType,
) : PagingSource<PageKeyType, ItemType>() {
    init {
        dataSnapshot.addInvalidationListener(::invalidate)

        registerInvalidatedCallback {
            dataSnapshot.removeInvalidationListener(::invalidate)
            dataSnapshot.invalidate()
        }

        if (!invalid && dataSnapshot.isInvalid) {
            invalidate()
        }
    }

    override fun getRefreshKey(state: PagingState<PageKeyType, ItemType>): PageKeyType? =
        startingPageKey

    override suspend fun load(params: LoadParams<PageKeyType>): LoadResult<PageKeyType, ItemType> {
        val pageKey = dataSnapshot.getPageKey(params.key ?: startingPageKey)

        return try {
            val items = dataSnapshot.getItems(pageKey.value)

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

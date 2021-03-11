package br.pedroso.remotemediatortest.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState

class CachePagingSource<KeyType : Any, Item : Any>(
    private val dataSnapshot: PagedCacheSnapshot<KeyType, Item>,
    private val startingPageKey: KeyType,
) : PagingSource<KeyType, Item>() {
    init {
        Log.d("LogTest", "Created $this")
        dataSnapshot.addInvalidationListener(::invalidate)

        registerInvalidatedCallback {
            Log.d("LogTest", "Invalidated $this")
            dataSnapshot.removeInvalidationListener(::invalidate)
            dataSnapshot.invalidate()
        }

        if (!invalid && dataSnapshot.isInvalid) {
            Log.d("LogTest", "Forced invalidation $this")
            invalidate()
        }
    }

    override fun getRefreshKey(state: PagingState<KeyType, Item>): KeyType? {
        return state.anchorPosition?.let { anchorPosition ->
            // This loads starting from previous page, but since PagingConfig.initialLoadSize spans
            // multiple pages, the initial load will still load items centered around
            // anchorPosition. This also prevents needing to immediately launch prepend due to
            // prefetchDistance.
            state.closestPageToPosition(anchorPosition)?.prevKey
        }.also { Log.d("LogTest", "getRefreshKey returned $it") }
    }

    override suspend fun load(params: LoadParams<KeyType>): LoadResult<KeyType, Item> {
        Log.d("LogTest", "Paging source started loading. Params: $params")
        val pageKey = dataSnapshot.getPageKey(params.key ?: startingPageKey)

        val callResult = runCatching { dataSnapshot.getItems(pageKey.value) }

        val items = callResult.getOrNull()
        val error = callResult.exceptionOrNull()

        return when {
            items != null -> LoadResult.Page(
                data = items,
                if (items.isEmpty() && params is LoadParams.Prepend) null else pageKey.previousPageKey,
                if (items.isEmpty() && params is LoadParams.Append) null else pageKey.nextPageKey
            ).also { Log.d("LogTest", "Loaded the following page: $it") }
            error != null -> LoadResult.Error(error)
            // This state should never happen but I'm adding it as a form of defensive programming.
            else -> LoadResult.Error(CachePagingSourceUnknownState())
        }.also { Log.d("LogTest", "Paging source loading finished with state: $it") }
    }

    private class CachePagingSourceUnknownState : Throwable()
}

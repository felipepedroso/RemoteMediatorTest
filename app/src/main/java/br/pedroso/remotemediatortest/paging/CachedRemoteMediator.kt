package br.pedroso.remotemediatortest.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import br.pedroso.remotemediatortest.entity.Item
import br.pedroso.remotemediatortest.entity.Page

@SuppressWarnings("TooManyFunctions")
@OptIn(ExperimentalPagingApi::class)
class CachedRemoteMediator(
    private val pagedCache: PagedCache<Int, Item, Int>,
    private val startingPageKey: Int,
    private val fetchPage: suspend (Int) -> Page
) : RemoteMediator<Int, Item>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Item>
    ): MediatorResult {
        Log.d("LogTest", "Mediator started loading with $loadType")
        return when (loadType) {
            LoadType.REFRESH -> refreshCache()
            LoadType.PREPEND -> prependPage(state)
            LoadType.APPEND -> appendPage(state)
        }.also {
            Log.d(
                "LogTest",
                "Mediator executed $loadType and returned $it. PageCache items: ${pagedCache.getItems().size}"
            )
        }
    }

    private suspend fun refreshCache(): MediatorResult {
        return updateCacheWithPage(startingPageKey, cleanPageCache = true)
    }

    private suspend fun appendPage(state: PagingState<Int, Item>): MediatorResult {
        val pageKey = getPageKeyForLastItem(state)
            ?: throw IllegalStateException("Cannot append a new page without having a last page.")

        val nextPageKey = pageKey.nextPageKey

        return if (nextPageKey != null) {
            updateCacheWithPage(nextPageKey)
        } else {
            MediatorResult.Success(endOfPaginationReached = true)
        }
    }

    private suspend fun prependPage(state: PagingState<Int, Item>): MediatorResult {
//        return MediatorResult.Success(endOfPaginationReached = true)
        val pageKey = getPageKeyForFirstItem(state)
            ?: throw IllegalStateException("Cannot prepend a new page without having a first page.")

        val previousPageKey = pageKey.previousPageKey

        return if (previousPageKey != null) {
            updateCacheWithPage(previousPageKey)
        } else {
            MediatorResult.Success(endOfPaginationReached = true)
        }
    }

    private suspend fun updateCacheWithPage(
        pageKeyValue: Int,
        cleanPageCache: Boolean = false
    ): MediatorResult {
        val fetchPageResult = runCatching { fetchPage(pageKeyValue) }

        val data = fetchPageResult.getOrNull()
        val error = fetchPageResult.exceptionOrNull()

        return when {
            data != null -> {
                val items = data.items
                val pageKey = PageKey(
                    value = data.currentPage,
                    previousPageKey = data.previousPage,
                    nextPageKey = data.nextPage
                )

                pagedCache.withTransaction {
                    if (cleanPageCache) {
                        clear()
                    }

                    if (items.isNotEmpty()) {
                        pagedCache.insert(items, pageKey)
                    }
                }

                MediatorResult.Success(endOfPaginationReached = items.isEmpty())
            }
            error != null -> MediatorResult.Error(error)
            // This state should never happen but I'm adding it as a form of defensive programming.
            else -> MediatorResult.Error(CachedRemoteMediatorUnknownState())
        }
    }

    private fun getPageKeyForLastItem(state: PagingState<Int, Item>): PageKey<Int>? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { item ->
            pagedCache.getPageKeyForItemId(item.id)
        }
    }

    private fun getPageKeyForFirstItem(state: PagingState<Int, Item>): PageKey<Int>? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { item ->
            pagedCache.getPageKeyForItemId(item.id)
        }
    }

    private class CachedRemoteMediatorUnknownState : Throwable()
}

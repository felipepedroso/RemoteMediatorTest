package br.pedroso.remotemediatortest.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import br.pedroso.remotemediatortest.debugLog

@SuppressWarnings("TooManyFunctions")
@OptIn(ExperimentalPagingApi::class)
class PagesCacheRemoteMediator<PageKeyType : Any, ItemType : Any, ItemIdType>(
    private val pagesCache: PagesCache<PageKeyType, ItemType, ItemIdType>,
    private val startingPageKey: PageKeyType,
    private val pageFetcher: PageFetcher<PageKeyType, ItemType>,
    private val itemIdGetter: ItemIdGetter<ItemType, ItemIdType>
) : RemoteMediator<PageKeyType, ItemType>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<PageKeyType, ItemType>
    ): MediatorResult {
        val pageKeyValue = when (loadType) {
            LoadType.REFRESH -> startingPageKey
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> state.lastItemOrNull()
                ?.let { lastItem -> itemIdGetter.getItemId(lastItem) }
                ?.let { itemId -> pagesCache.getPageKeyForItemId(itemId)?.nextPageKey }
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val page = pageFetcher.fetchPage(pageKeyValue)

            pagesCache.Transaction {
                if (loadType == LoadType.REFRESH) {
                    clear()
                }

                if (page.items.isNotEmpty()) {
                    insert(page.items, page.key)
                }
            }

            MediatorResult.Success(endOfPaginationReached = page.items.isEmpty())
        } catch (error: Throwable) {
            MediatorResult.Error(error)
        }.also { result ->
            debugLog("--------")
            debugLog("Mediator request load type: $loadType")
            debugLog("Mediator request state: $state")
            debugLog("Mediator request result: $result")

            if (result is MediatorResult.Success) {
                debugLog("Successful Mediator result, endOfPaginationReached=${result.endOfPaginationReached}")
            } else if (result is MediatorResult.Error) {
                debugLog("Madiator failed with: ${result.throwable}")
            }

        }
    }
}

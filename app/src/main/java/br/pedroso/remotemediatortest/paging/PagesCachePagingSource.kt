package br.pedroso.remotemediatortest.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import br.pedroso.remotemediatortest.debugLog

class PagesCachePagingSource<PageKeyType : Any, ItemType : Any, ItemIdType>(
    private val cache: Map<PageKeyType, List<ItemType>>,
    private val pageKeysIndex: HashMap<PageKeyType, PageKey<PageKeyType>>,
    private val startingPageKey: PageKeyType,
    private val itemIdGetter: ItemIdGetter<ItemType, ItemIdType>,
    private val itemPageKeys: Map<ItemIdType, PageKey<PageKeyType>>
) : PagingSource<PageKeyType, ItemType>() {

    override fun getRefreshKey(state: PagingState<PageKeyType, ItemType>): PageKeyType? =
        state.anchorPosition?.let { anchorPosition ->
            val closestItem = state.closestItemToPosition(anchorPosition) ?: return@let startingPageKey
            val closestItemId = itemIdGetter.getItemId(closestItem)
            itemPageKeys[closestItemId]?.value ?: startingPageKey
        }

    override suspend fun load(params: LoadParams<PageKeyType>): LoadResult<PageKeyType, ItemType> {
        return try {
            val pageKey = pageKeysIndex[params.key] ?: pageKeysIndex.getValue(startingPageKey)
            val items = cache.getValue(pageKey.value)

            LoadResult.Page(
                data = items,
                pageKeysIndex[pageKey.previousPageKey]?.value,
                pageKeysIndex[pageKey.nextPageKey]?.value
            )

        } catch (error: Throwable) {
            LoadResult.Error(error)
        }.also { result ->
            debugLog("--------")
            debugLog("Paging source params: $params")
            debugLog("Paging source params key: ${params.key}")
            debugLog("Paging source params loadSize: ${params.loadSize}")
            debugLog("Paging source params placeholderEnabled: ${params.placeholdersEnabled}")
            debugLog("Paging source result: $result")

            if (result is LoadResult.Page) {
                debugLog("Paging source page data: ${result.data}")
                debugLog("Paging source page prevKey: ${result.prevKey}")
                debugLog("Paging source page nextKey: ${result.nextKey}")
                debugLog("Paging source page itemsBefore: ${result.itemsBefore}")
                debugLog("Paging source page itemsAfter: ${result.itemsAfter}")
            } else if (result is LoadResult.Error) {
                debugLog("Paging source error: ${result.throwable}")
            }
        }
    }
}

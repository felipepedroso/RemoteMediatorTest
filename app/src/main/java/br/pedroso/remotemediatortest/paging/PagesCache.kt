package br.pedroso.remotemediatortest.paging

import br.pedroso.remotemediatortest.debugLog
import java.util.concurrent.atomic.AtomicBoolean

class PagesCache<PageKeyType : Any, ItemType : Any, ItemIdType>(
    private val itemIdGetter: ItemIdGetter<ItemType, ItemIdType>,
    private val startingPageKey: PageKeyType,
) {

    private val cache = HashMap<PageKeyType, LinkedHashMap<ItemIdType, ItemType>>()

    private val itemPageKeys = HashMap<ItemIdType, PageKey<PageKeyType>>()

    private val pageKeysIndex = HashMap<PageKeyType, PageKey<PageKeyType>>()

    override fun toString(): String {
        return "Cache: $cache\nitemPageKeys: $itemPageKeys\npageKeysIndex: $pageKeysIndex"
    }

    private fun getPage(pageKey: PageKeyType): HashMap<ItemIdType, ItemType> {
        return cache.getOrPut(pageKey) { linkedMapOf() }
    }

    fun getItems(): List<ItemType> = cache.values.flatMap { it.values }

    fun getItems(page: PageKeyType): List<ItemType> {
        return cache[page]?.values?.toList().orEmpty()
    }

    fun getPageKeyForItemId(itemId: ItemIdType): PageKey<PageKeyType>? {
        return itemPageKeys[itemId]
    }

    fun getPageKey(keyValue: PageKeyType): PageKey<PageKeyType> {
        return pageKeysIndex.getOrPut(keyValue) { PageKey(keyValue) }
    }

    val pagingSourceFactory = InvalidatingPagingSourceFactory(::createPagingSource)

    private fun createPagingSource(): PagesCachePagingSource<PageKeyType, ItemType> {
        val dataSnapshot: Map<PageKeyType, List<ItemType>> = cache.map { (key, value) ->
            key to value.values.toList()
        }.toMap()

        val pageKeysIndex = HashMap(pageKeysIndex)

        return PagesCachePagingSource(dataSnapshot, pageKeysIndex, startingPageKey)
    }

    inner class Transaction constructor(block: Transaction.() -> Unit) {
        private val pendingInvalidation = AtomicBoolean(false)

        init {
            block(this)
            if (pendingInvalidation.compareAndSet(true, false)) {
                pagingSourceFactory.invalidate()
            }
        }

        fun insert(item: ItemType, pageKey: PageKey<PageKeyType>) {
            debugLog("----")
            debugLog("Before inserting: ${this@PagesCache}")
            val page = getPage(pageKey.value)
            val itemId = itemIdGetter.getItemId(item)
            page[itemId] = item
            itemPageKeys[itemId] = pageKey
            pageKeysIndex[pageKey.value] = pageKey
            pendingInvalidation.set(true)
            debugLog("After inserting: ${this@PagesCache}")
        }

        fun insert(items: List<ItemType>, pageKey: PageKey<PageKeyType>) =
            items.forEach { item -> insert(item, pageKey) }

        fun replace(originalItem: ItemType, newItem: ItemType) {
            val itemId = itemIdGetter.getItemId(originalItem)
            val pageKey = itemPageKeys[itemId]

            if (pageKey != null) {
                val page = getPage(pageKey.value)
                page[itemId] = newItem
            }
            pendingInvalidation.set(true)
        }

        fun delete(item: ItemType) {
            val itemId = itemIdGetter.getItemId(item)
            val pageKey = itemPageKeys[itemId]

            if (pageKey != null) {
                val page = getPage(pageKey.value)
                page.remove(itemId)
                itemPageKeys.remove(itemId)
                pendingInvalidation.set(true)
            }
        }

        fun clear() {
            cache.clear()
            itemPageKeys.clear()
            pageKeysIndex.clear()
            pendingInvalidation.set(true)
        }
    }
}

package br.pedroso.remotemediatortest.paging

import java.util.concurrent.atomic.AtomicBoolean

class PagesCache<PageKeyType, ItemType, ItemIdType>(
    private val itemIdGetter: ItemIdGetter<ItemType, ItemIdType>,
) {

    private val cache = HashMap<PageKeyType, LinkedHashMap<ItemIdType, ItemType>>()

    private val itemPageKeys = HashMap<ItemIdType, PageKey<PageKeyType>>()

    private val pageKeysIndex = HashMap<PageKeyType, PageKey<PageKeyType>>()

    private val snapshots = mutableListOf<PagesCacheSnapshot<PageKeyType, ItemType>>()

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

    fun createSnapshot(): PagesCacheSnapshot<PageKeyType, ItemType> {
        val dataSnapshot: Map<PageKeyType, List<ItemType>> = cache.map { (key, value) ->
            key to value.values.toList()
        }.toMap()

        return PagesCacheSnapshot(dataSnapshot, HashMap(pageKeysIndex)).also { snapshot ->
            snapshots.add(snapshot)
        }
    }

    inner class Transaction constructor(block: Transaction.() -> Unit) {
        private val pendingInvalidation = AtomicBoolean(false)

        init {
            block(this)
            if (pendingInvalidation.compareAndSet(true, false)) {
                invalidateSnapshots()
            }
        }

        private fun invalidateSnapshots() {
            snapshots.forEach { it.invalidate() }
            snapshots.clear()
        }

        fun insert(item: ItemType, pageKey: PageKey<PageKeyType>) {
            val page = getPage(pageKey.value)
            val itemId = itemIdGetter.getItemId(item)
            page[itemId] = item
            itemPageKeys[itemId] = pageKey
            pageKeysIndex[pageKey.value] = pageKey
            pendingInvalidation.set(true)
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
        }

        fun delete(item: ItemType) {
            val itemId = itemIdGetter.getItemId(item)
            val pageKey = itemPageKeys[itemId]

            if (pageKey != null) {
                val page = getPage(pageKey.value)
                page.remove(itemId)
                itemPageKeys.remove(itemId)
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

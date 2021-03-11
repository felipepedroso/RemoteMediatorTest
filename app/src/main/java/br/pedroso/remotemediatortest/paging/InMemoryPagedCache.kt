package br.pedroso.remotemediatortest.paging

class InMemoryPagedCache<Key, Item, ItemId>(
    val getItemId: Item.() -> ItemId
) : PagedCache<Key, Item, ItemId> {

    private val cache = HashMap<Key, LinkedHashMap<ItemId, Item>>()

    private val itemPageKeys = HashMap<ItemId, PageKey<Key>>()

    private val pageKeysIndex = HashMap<Key, PageKey<Key>>()

    private fun getPage(pageKey: Key): HashMap<ItemId, Item> {
        return cache.getOrPut(pageKey) { linkedMapOf() }
    }

    private fun registerItem(item: Item, pageKey: PageKey<Key>) {
        val page = getPage(pageKey.value)
        page[item.getItemId()] = item
        itemPageKeys[item.getItemId()] = pageKey
        pageKeysIndex[pageKey.value] = pageKey
    }

    override fun insert(item: Item, pageKey: PageKey<Key>) {
        registerItem(item, pageKey)
    }

    override fun insert(items: List<Item>, pageKey: PageKey<Key>) {
        items.forEach { registerItem(it, pageKey) }
    }

    override fun replace(originalItem: Item, newItem: Item) {
        val itemId = originalItem.getItemId()
        val pageKey = itemPageKeys[itemId]

        if (pageKey != null) {
            val page = getPage(pageKey.value)
            page[itemId] = newItem
        }
    }

    override fun delete(item: Item) {
        val itemId = item.getItemId()
        val pageKey = itemPageKeys[itemId]

        if (pageKey != null) {
            val page = getPage(pageKey.value)
            page.remove(itemId)
            itemPageKeys.remove(itemId)
        }
    }

    override fun getItems(): List<Item> = cache.values.flatMap { it.values }

    override fun getItems(page: Key): List<Item> {
        return cache[page]?.values?.toList().orEmpty()
    }

    override fun getPageKeyForItemId(itemId: ItemId): PageKey<Key>? {
        return itemPageKeys[itemId]
    }

    override fun clear() {
        cache.clear()
        itemPageKeys.clear()
        pageKeysIndex.clear()
    }

    override fun getPageKey(keyValue: Key): PageKey<Key> {
        return pageKeysIndex.getOrPut(keyValue) { PageKey(keyValue) }
    }

    override fun getSnapshot(): PagedCacheSnapshot<Key, Item> {
        val dataSnapshot: Map<Key, List<Item>> = cache.map {
            it.key to it.value.values.toList()
        }.toMap()

        val snapshot = InMemoryPagedCacheSnapshot(dataSnapshot, pageKeysIndex)
        snapshots.add(snapshot)
        return snapshot
    }

    private val snapshots = mutableListOf<PagedCacheSnapshot<Key, Item>>()

    private fun invalidateSnapshots() {
        snapshots.forEach { it.invalidate() }
        snapshots.clear()
    }

    override fun withTransaction(block: PagedCache<Key, Item, ItemId>.() -> Unit) {
        block.invoke(this)
        invalidateSnapshots()
        dataChangedListeners.forEach { it.onDataChanged() }
    }

    private val dataChangedListeners = mutableListOf<PagedCache.OnDataChangedListener>()

    override fun addOnDataChangedListener(listener: PagedCache.OnDataChangedListener) {
        if (!dataChangedListeners.contains(listener)) {
            dataChangedListeners.add(listener)
        }
    }

    override fun removeOnDataChangedListener(listener: PagedCache.OnDataChangedListener) {
        if (dataChangedListeners.contains(listener)) {
            dataChangedListeners.remove(listener)
        }
    }
}

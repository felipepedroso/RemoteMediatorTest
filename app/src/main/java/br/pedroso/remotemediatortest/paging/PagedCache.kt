package com.cookpad.android.coreandroid.paging

@SuppressWarnings("TooManyFunctions")
interface PagedCache<KeyType, Item, ItemId> {
    fun insert(item: Item, pageKey: PageKey<KeyType>)
    fun insert(items: List<Item>, pageKey: PageKey<KeyType>)
    fun replace(originalItem: Item, newItem: Item)
    fun delete(item: Item)
    fun getItems(): List<Item>
    fun getItems(page: KeyType): List<Item>
    fun getPageKeyForItemId(itemId: ItemId): PageKey<KeyType>?
    fun clear()
    fun getPageKey(keyValue: KeyType): PageKey<KeyType>
    fun getSnapshot(): PagedCacheSnapshot<KeyType, Item>
    fun withTransaction(block: PagedCache<KeyType, Item, ItemId>.() -> Unit)

    fun addOnDataChangedListener(listener: OnDataChangedListener)
    fun removeOnDataChangedListener(listener: OnDataChangedListener)

    fun interface OnDataChangedListener {
        fun onDataChanged()
    }
}

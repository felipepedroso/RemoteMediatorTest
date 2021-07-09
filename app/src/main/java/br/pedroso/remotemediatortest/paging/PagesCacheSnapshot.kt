package br.pedroso.remotemediatortest.paging

import java.util.concurrent.atomic.AtomicBoolean

class PagesCacheSnapshot<PageKeyType, ItemType>(
    private val cache: Map<PageKeyType, List<ItemType>>,
    private val pageKeysIndex: HashMap<PageKeyType, PageKey<PageKeyType>>,
) {

    private val _invalid = AtomicBoolean(false)

    private val invalidationListeners = mutableListOf<PagesCacheSnapshotOnInvalidatedListener>()

    fun getItems(page: PageKeyType): List<ItemType> = cache[page].orEmpty()

    fun getPageKey(keyValue: PageKeyType): PageKey<PageKeyType> {
        return pageKeysIndex.getOrPut(keyValue) { PageKey(keyValue) }
    }

    val isInvalid: Boolean
        get() = _invalid.get()

    fun invalidate() {
        if (_invalid.compareAndSet(false, true)) {
            invalidationListeners.forEach { it.onInvalidated() }
        }
    }

    fun addInvalidationListener(listener: PagesCacheSnapshotOnInvalidatedListener) {
        if (!invalidationListeners.contains(listener)) {
            invalidationListeners.add(listener)
        }
    }

    fun removeInvalidationListener(listener: PagesCacheSnapshotOnInvalidatedListener) {
        if (invalidationListeners.contains(listener)) {
            invalidationListeners.remove(listener)
        }
    }
}

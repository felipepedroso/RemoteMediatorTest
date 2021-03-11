package br.pedroso.remotemediatortest.paging

import androidx.annotation.AnyThread

interface PagedCacheSnapshot<KeyType, Item> {
    fun getItems(page: KeyType): List<Item>

    fun getPageKey(keyValue: KeyType): PageKey<KeyType>

    val isInvalid: Boolean

    @AnyThread
    fun invalidate()

    @AnyThread
    fun addInvalidationListener(listener: InMemoryPagedCacheSnapshot.InvalidationListener)

    @AnyThread
    fun removeInvalidationListener(listener: InMemoryPagedCacheSnapshot.InvalidationListener)

    fun interface InvalidationListener {
        fun onInvalidated()
    }
}

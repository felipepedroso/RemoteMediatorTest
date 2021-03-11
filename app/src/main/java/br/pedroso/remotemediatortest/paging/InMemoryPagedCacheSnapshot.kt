package br.pedroso.remotemediatortest.paging

import android.util.Log
import androidx.annotation.AnyThread
import java.util.concurrent.atomic.AtomicBoolean

class InMemoryPagedCacheSnapshot<Key, Item>(
    private val cache: Map<Key, List<Item>>,
    private val pageKeysIndex: HashMap<Key, PageKey<Key>>,
) : PagedCacheSnapshot<Key, Item> {

    init {
        Log.d("LogTest", "Created snapshot $this with ${cache.size} items.")
    }

    private val _invalid = AtomicBoolean(false)

    private val invalidationListeners = mutableListOf<InvalidationListener>()

    override fun getItems(page: Key): List<Item> = cache[page].orEmpty()

    override fun getPageKey(keyValue: Key): PageKey<Key> {
        return pageKeysIndex.getOrPut(keyValue) { PageKey(keyValue) }
    }

    override val isInvalid: Boolean
        get() = _invalid.get()

    @AnyThread
    override fun invalidate() {
        if (_invalid.compareAndSet(false, true)) {
            Log.d("LogTest", "Invalidated snapshot $this with ${cache.size} pages.")
            invalidationListeners.forEach { it.onInvalidated() }
        }
    }

    @AnyThread
    override fun addInvalidationListener(listener: InvalidationListener) {
        if (!invalidationListeners.contains(listener)) {
            invalidationListeners.add(listener)
        }
    }

    @AnyThread
    override fun removeInvalidationListener(listener: InvalidationListener) {
        if (invalidationListeners.contains(listener)) {
            invalidationListeners.remove(listener)
        }
    }

    fun interface InvalidationListener {
        fun onInvalidated()
    }
}

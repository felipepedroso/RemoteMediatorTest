package br.pedroso.remotemediatortest.paging

import androidx.paging.PagingSource

class InvalidatingPagingSourceFactory<PageKeyType : Any, ItemType : Any>(
    private val createPagingSourceCallback: () -> PagingSource<PageKeyType, ItemType>
) : () -> PagingSource<PageKeyType, ItemType> {
    private val pagingSources = mutableListOf<PagingSource<PageKeyType, ItemType>>()

    override fun invoke(): PagingSource<PageKeyType, ItemType> =
        createPagingSourceCallback.invoke().also { pagingSources.add(it) }

    fun invalidate() {
        pagingSources.toList().forEach { pagingSource ->
            if (!pagingSource.invalid) {
                pagingSource.invalidate()
            }
        }

        pagingSources.removeAll { it.invalid }
    }
}

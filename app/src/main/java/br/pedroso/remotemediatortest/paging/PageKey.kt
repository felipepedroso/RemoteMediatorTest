package br.pedroso.remotemediatortest.paging

data class PageKey<KeyType>(
    val value: KeyType,
    val previousPageKey: KeyType? = null,
    val nextPageKey: KeyType? = null,
) {
    fun hasNext() = nextPageKey != null
    fun hasPrevious() = previousPageKey != null
}

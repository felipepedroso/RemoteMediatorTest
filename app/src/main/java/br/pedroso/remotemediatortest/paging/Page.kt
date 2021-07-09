package br.pedroso.remotemediatortest.paging

data class Page<PageKeyType, ItemType>(
    val key: PageKey<PageKeyType>,
    val items: List<ItemType>,
)

package br.pedroso.remotemediatortest.paging

fun interface ItemIdGetter<ItemType, ItemIdType> {
    fun getItemId(item: ItemType): ItemIdType
}

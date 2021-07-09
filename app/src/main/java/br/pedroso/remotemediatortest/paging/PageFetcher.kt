package br.pedroso.remotemediatortest.paging

fun interface PageFetcher<PageKeyType, ItemType> {
    suspend fun fetchPage(pageNumber: PageKeyType): Page<PageKeyType, ItemType>
}

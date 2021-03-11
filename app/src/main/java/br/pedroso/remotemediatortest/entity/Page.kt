package br.pedroso.remotemediatortest.entity

data class Page(
    val currentPage: Int,
    val nextPage: Int?,
    val previousPage: Int?,
    val items: List<Item>
)

package br.pedroso.remotemediatortest.api

import br.pedroso.remotemediatortest.entity.Item
import br.pedroso.remotemediatortest.entity.Page
import kotlinx.coroutines.delay
import kotlin.random.Random

class ItemsApi {
    suspend fun getItemsPage(pageNumber: Int): Page {
        delay(DELAY)
        if (pageNumber < 1) error("API doesn't accept pageNumbers smaller than 1.")

        if (Random.nextInt(100) > 90) {
            error("Simulating a random API error.")
        }

        return Page(
            currentPage = pageNumber,
            nextPage = pageNumber + 1,
            previousPage = if (pageNumber > 1) null else pageNumber - 1,
            items = (1..10).map { count ->
                val id = count + ((pageNumber - 1) * 10)
                Item(
                    id = id,
                    description = "Page $pageNumber - Item $id"
                )
            }
        )
    }

    companion object {
        private const val DELAY = 500L
    }
}

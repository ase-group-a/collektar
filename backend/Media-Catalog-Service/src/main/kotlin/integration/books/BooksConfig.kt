package integration.books

import io.ktor.server.application.*

data class BooksConfig(
    val bookApiKey: String,
    val baseUrl: String
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): BooksConfig {
            val bookKey = System.getenv("GOOGLE_BOOKSAPI_KEY")
                ?: env.config.propertyOrNull("books.bookApiKey")?.getString()
                ?: error("GOOGLE_BOOKSAPI_KEY not set")

            val url = System.getenv("GOOGLE_BOOKS_BASE_URL")
                ?: env.config.propertyOrNull("books.baseUrl")?.getString()
                ?: error("GOOGLE_BOOKS_BASE_URL not set correctly")

            return BooksConfig(bookKey, url)
        }
    }
}
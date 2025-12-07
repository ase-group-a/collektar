package integration.books

import io.ktor.server.application.*
import com.collektar.config.ConfigUtils.getConfigValue

data class BooksConfig(
    val bookApiKey: String,
    val baseUrl: String
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): BooksConfig {
            return BooksConfig(
                bookApiKey = getConfigValue(env, "GOOGLE_BOOKSAPI_KEY", "books.bookApiKey"),
                baseUrl = getConfigValue(env, "GOOGLE_BOOKS_BASE_URL", "books.baseUrl")
            )
        }
    }
}
package di.modules

import controllers.BooksController
import controllers.Controller
import integration.books.BooksClient
import integration.books.BooksClientImpl
import integration.books.BooksConfig
import org.koin.dsl.module
import org.koin.core.qualifier.named
import service.BooksService
import io.ktor.server.application.*

const val BOOKS_CONFIG_NAME = "books_config"
const val BOOKS_CONTROLLER_NAME = "books"

fun booksModule(env: ApplicationEnvironment) = module {

    single(named(BOOKS_CONFIG_NAME)) { BooksConfig.fromEnv(env) }

    single<BooksClient> {
        BooksClientImpl(
            get(),
            get(named(BOOKS_CONFIG_NAME))
        )
    }

    single { BooksService(get(), get()) }

    single<Controller>(named(BOOKS_CONTROLLER_NAME)) { BooksController(get()) }
}
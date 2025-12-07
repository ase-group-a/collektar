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
import integration.books.GoogleBooksSearchResponse

fun booksModule(env: ApplicationEnvironment) = module {

    single { BooksConfig.fromEnv(env) }

    single<BooksClient> { BooksClientImpl(get(), get()) }

    single { BooksService(get()) }

    single<Controller>(named("books")) { BooksController(get()) }
}

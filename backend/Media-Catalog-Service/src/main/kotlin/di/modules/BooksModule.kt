package di.modules

import controllers.BookController
import controllers.Controller
import integration.google.*
import org.koin.dsl.module
import service.BookService
import io.ktor.server.application.*

fun googleBooksModule(env: ApplicationEnvironment) = module {

    single { GoogleConfig.fromEnv(env) }

    single<BooksClient> { BooksClientImpl(get(), get()) }

    single { BookService(get()) }

    single<Controller>(named("book")) { BookController(get()) }
}

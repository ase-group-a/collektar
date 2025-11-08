package di

import controllers.Controller
import controllers.ControllerRegistry
import controllers.MusicController
import org.koin.dsl.module

val controllerModule = module {
    single<Controller> { MusicController(get()) }
    single { ControllerRegistry(getAll()) }
}

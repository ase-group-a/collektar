package di

import com.collektar.HttpProvider
import controllers.Controller
import controllers.ControllerRegistry
import org.koin.dsl.module

val coreModule = module {
    single { HttpProvider.client }
    single {
        ControllerRegistry(
            musicService = get(),
            movieService = get()
        )
    }
}
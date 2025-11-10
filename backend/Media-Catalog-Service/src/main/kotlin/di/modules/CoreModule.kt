package di

import com.collektar.HttpProvider
import controllers.Controller
import controllers.ControllerRegistry
import org.koin.dsl.module

val coreModule = module {
    single { HttpProvider.client }
    single { ControllerRegistry(getAll<Controller>()) }

    // Add other global singletons here that are relevant for all APIs
}

package di

import service.MusicService
import org.koin.dsl.module

val serviceModule = module {
    single { MusicService(get()) }
}

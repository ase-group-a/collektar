package di

import com.collektar.HttpProvider
import integration.spotify.SpotifyClient
import integration.spotify.SpotifyClientImpl
import integration.spotify.SpotifyConfig
import integration.spotify.SpotifyTokenCache
import org.koin.dsl.module

val clientModule = module {
    single { HttpProvider.client }

    single { SpotifyTokenCache() }

    single<SpotifyClient> {
        SpotifyClientImpl(
            httpClient = get(),
            clientId = get<SpotifyConfig>().clientId,
            clientSecret = get<SpotifyConfig>().clientSecret,
            tokenUrl = get<SpotifyConfig>().tokenUrl,
            baseUrl = get<SpotifyConfig>().baseUrl,
            tokenCache = get()
        )
    }
}

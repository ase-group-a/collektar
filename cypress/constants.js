export const USER = {
    email: "user@example.com",
    username: "exampleUser",
    displayName: "Display Name",
    password: "1234AbCdE!_?"
}

export const TITLE_BAR_SETTINGS = {
    logoSrc: "/collektar-logo.svg",
    games: {
        name: "Games",
        href: "/media/games"
    },
    movies: {
        name: "Movies",
        href: "/media/movies"
    },
    shows: {
        name: "Shows",
        href: "/media/shows"
    },
    music: {
        name: "Music",
        href: "/media/music"
    },
    books: {
        name: "Books",
        href: "/media/books"
    },
    login: {
        name: "Login",
        href: "/login"
    }
}

export const GAME_SEARCH = {
    searchTerm: "control ultimate edition", // One of the games where the IGDB search does not return many irrelevant games.
    imageSrc: "/api/media/images?source=igdb&id=co2ewb",
    gameTitle: "Control: Ultimate Edition"
}

export const MOVIE_SEARCH = {
    searchTerm: "morbius",
    imageSrc: "/api/media/images?source=tmbd&id=6JjfSchsU6daXk2AKX8EEBjO3Fm",
    movieTitle: "Morbius"
}

export const SHOW_SEARCH = {
    searchTerm: "house",
    imageSrc: "/api/media/images?source=tmbd&id=3Cz7ySOQJmqiuTdrc6CY0r65yDI",
    showTitle: "House"
}

export const MUSIC_SEARCH = {
    searchTerm: "rick astley",
    imageSrc: "/api/media/images?source=spotify&id=ab67616d0000b27315ebbedaacef61af244262a8",
    songTitle: "Never Gonna Give You Up"
}

export const BOOK_SEARCH = {
    searchTerm: "best practice software engineering schatten",
    imageSrc: /\/api\/media\/images\?source=google_books&id=*/,
    bookTitle: "Best Practice Software-Engineering"
}
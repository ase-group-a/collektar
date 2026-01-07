import Link from 'next/link'

export default function Page() {
    return (
        <div className=" min-h-[75dvh] flex items-center justify-center">
            <main className="w-full max-w-5xl text-center">
                <header className="mb-6">
                    <img
                        src="/collektar-logo.svg"
                        alt="Collektar logo"
                        className="mx-auto h-24 sm:h-28 mb-4"
                    />
                    <p className="text-sm opacity-80">
                        Collect, organize and manage your media like movies, series, games, music and books — in one library.
                    </p>
                </header>

                <section className="mt-6">
                    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-3">
                        <Link href="/media/games" className="btn btn-lg hover:border-white">Games</Link>
                        <Link href="/media/movies" className="btn btn-lg hover:border-white">Movies</Link>
                        <Link href="/media/shows" className="btn btn-lg hover:border-white">Shows</Link>
                        <Link href="/media/music" className="btn btn-lg hover:border-white">Music</Link>
                        <Link href="/media/books" className="btn btn-lg hover:border-white hover:border-white">Books</Link>
                        <Link href="/media/boardgames" className="btn btn-lg hover:border-white hover:border-white">Boardgames</Link>
                    </div>

                    <div className="mt-5 flex items-center justify-center gap-3">
                        <Link href="/login" className="btn btn-primary btn-sm text-black">Log in</Link>
                        <Link href="/register" className="btn btn-outline btn-sm">Sign up</Link>
                    </div>
                </section>

                <section className="mt-10 grid grid-cols-1 sm:grid-cols-2">
                    <div className="card p-4">
                        <div className="font-semibold">Search public APIs</div>
                        <div className="text-xs opacity-80">
                            Find metadata and cover art with public API integrations.
                        </div>
                    </div>
                    <div className="card p-4">
                        <div className="font-semibold">Organize</div>
                        <div className="text-xs opacity-80">
                            Create custom collections with your own custom items.
                        </div>
                    </div>
                </section>

                <footer className="mt-10 text-[13px] opacity-70">
                    <div>
                        Made for a university project •{' '}
                        <a
                            href="https://github.com/ase-group-a/collektar"
                            className="link"
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            Repository
                        </a>
                    </div>
                </footer>
            </main>
        </div>
    )
}

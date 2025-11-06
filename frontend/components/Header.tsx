"use client";
import Link from "next/link";
import { UserIcon } from "lucide-react";

export default function Header() {
    return (
        <header className="bg-neutral text-neutral-content m-6">
            <div className="container mx-auto px-6 flex items-center justify-between h-16">
                <Link href="/" className="flex items-center gap-2">
                    <img
                        src="/collektar-logo.svg"
                        alt="Collektar logo"
                        className="w-50 h-50"
                    />
                </Link>

                <nav className="hidden md:flex items-center gap-10 text-lg font-medium">
                    <Link href="/games" className="hover:text-primary transition-colors">
                        Games
                    </Link>
                    <Link href="/films" className="hover:text-primary transition-colors">
                        Films / Series
                    </Link>
                    <Link href="/music" className="hover:text-primary transition-colors">
                        Music
                    </Link>
                    <Link href="/books" className="hover:text-primary transition-colors">
                        Books
                    </Link>
                </nav>

                <div className="flex items-center gap-2">
                    <Link
                        href="/login"
                        className="flex items-center gap-2 hover:text-primary transition-colors"
                    >
                        <span>Login</span>
                        <UserIcon size={20} />
                    </Link>
                </div>
            </div>
        </header>
    );
}

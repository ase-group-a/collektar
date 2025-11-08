"use client";
import Link from "next/link";
import { UserIcon } from "lucide-react";

export default function Header() {
    return (
        <div className="navbar bg-base-200 shadow-md px-6 py-5 relative">
            <div className="flex-none">
                <Link href="/">
                    <img
                        src="/collektar-logo.svg"
                        alt="Collektar logo"
                        className="w-45"
                    />
                </Link>
            </div>

            <div className="absolute left-1/2 transform -translate-x-1/2 hidden md:flex ">
                <ul className="menu menu-horizontal text-lg font-medium gap-x-8">
                    <li><Link href="/media/games">Games</Link></li>
                    <li><Link href="/media/movies">Movies</Link></li>
                    <li><Link href="/media/shows">Shows</Link></li>
                    <li><Link href="/media/music">Music</Link></li>
                    <li><Link href="/media/books">Books</Link></li>
                </ul>
            </div>

            <div className="flex-none ml-auto">
                <Link href="/login" className="btn btn-ghost gap-2">
                    <UserIcon size={20} />
                    Login
                </Link>
            </div>
        </div>
    );
}

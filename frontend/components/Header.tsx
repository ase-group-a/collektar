"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth/AuthProvider";
import { UserCircleIcon } from "@heroicons/react/24/outline";
import { useRouter } from "next/navigation";

export default function Header() {
    const { user, isAuthenticated, logout } = useAuth();
    const router = useRouter();

    const handleLogout = async () => {
        try {
            await logout();
            router.push("/login");
        } catch (err) {
            console.error("Logout failed", err);
        }
    };


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

            <div className="absolute left-1/2 transform -translate-x-1/2 hidden md:flex">
                <ul className="menu menu-horizontal text-lg font-medium gap-x-8">
                    <li><Link href="/media/games">Games</Link></li>
                    <li><Link href="/media/movies">Movies</Link></li>
                    <li><Link href="/media/shows">Shows</Link></li>
                    <li><Link href="/media/music">Music</Link></li>
                    <li><Link href="/media/books">Books</Link></li>
                    <li><Link href="/media/boardgames">Board Games</Link></li>
                </ul>
            </div>

            <div className="flex-none ml-auto">
                {!isAuthenticated && (
                    <Link href="/login" className="btn btn-ghost gap-2">
                        <UserCircleIcon className="w-5 h-5" />
                        Login
                    </Link>
                )}

                {isAuthenticated && (
                    <div className="dropdown dropdown-end">
                        <label tabIndex={0} className="btn btn-ghost gap-2 cursor-pointer">
                            <UserCircleIcon className="w-6 h-6" />
                            <span>{user?.display_name ?? "Account"}</span>
                        </label>

                        <ul
                            tabIndex={0}
                            className="dropdown-content menu bg-base-100 rounded-box shadow-lg w-48 mt-3"
                        >
                            <li>
                                <Link href={`/u/${user?.username}`}>My Collections</Link>
                            </li>
                            <li>
                                <Link href="/settings/account">Account</Link>
                            </li>

                            <li>
                                <button onClick={handleLogout} className="text-red-500">
                                    Logout
                                </button>
                            </li>
                        </ul>
                    </div>
                )}
            </div>
        </div>
    );
}

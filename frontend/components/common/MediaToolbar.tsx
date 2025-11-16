"use client";
import { Bars3Icon, Squares2X2Icon } from "@heroicons/react/24/solid";
import SearchBar from "@/components/common/SearchBar";

interface MediaToolbarProps {
    query: string;
    setQuery: (q: string) => void;
    viewMode: "grid" | "list";
    setViewMode: (mode: "grid" | "list") => void;
    resetPage: () => void;
}

const MediaToolbar = ({ query, setQuery, viewMode, setViewMode, resetPage }: MediaToolbarProps) => {
    return (
        <div className="pb-5 flex justify-between items-center">
            <SearchBar
                initialQuery={query}
                onSearch={(q) => {
                    setQuery(q);
                    resetPage();
                }}
            />

            <div className="flex gap-2">
                <button
                    aria-label="List View"
                    onClick={() => setViewMode("list")}
                    className={`btn btn-sm ${viewMode === "list" ? "btn-active" : ""}`}
                >
                    <Bars3Icon className="w-5 h-5" />
                </button>
                <button
                    aria-label="Grid View"
                    onClick={() => setViewMode("grid")}
                    className={`btn btn-sm ${viewMode === "grid" ? "btn-active" : ""}`}
                >
                    <Squares2X2Icon className="w-5 h-5" />
                </button>
            </div>
        </div>
    );
};

export default MediaToolbar;

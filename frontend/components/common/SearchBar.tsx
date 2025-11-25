"use client";
import { useState, useCallback } from "react";

interface SearchBarProps {
    initialQuery?: string;
    onSearch: (query: string) => void;
}

const SearchBar = ({ initialQuery = "", onSearch }: SearchBarProps) => {
    const [input, setInput] = useState(initialQuery);

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            onSearch(input);
        }
    };

    return (
        <label className="input">
            <svg
                className="h-[1em] opacity-50"
                viewBox="0 0 24 24"
            >
                <g strokeLinejoin="round" strokeLinecap="round" strokeWidth="2.5" fill="none" stroke="currentColor">
                    <circle cx="11" cy="11" r="8"></circle>
                    <path d="m21 21-4.3-4.3"></path>
                </g>
            </svg>
            <input
                type="search"
                className=" "
                placeholder="Search"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
            />
        </label>
    );
};

export default SearchBar;

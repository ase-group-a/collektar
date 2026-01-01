"use client";
import React from "react";
import Link from "next/link";

type Props = {
    type: string;
    hidden?: boolean;
    onToggle?: () => void;
    editMode?: boolean;
    username: string;
};

export default function CollectionCard({ type, hidden, onToggle, editMode, username }: Props) {
    const slug = type.toLowerCase();

    return (
        <div
            className={`card bg-base-100 shadow-md flex flex-col justify-between relative min-h-[80px] ${
                hidden ? "opacity-60" : "opacity-100"
            }`}
        >
            <div className="p-4">
                <Link href={`/u/${username}/${slug}`} className="no-underline text-inherit">
                    <h3 className="text-base font-semibold">{type}</h3>
                </Link>
                <div className="text-xs text-gray-500 mt-1">{hidden ? "hidden" : "visible"}</div>
            </div>

            {editMode && (
                <div className="p-4 flex justify-end">
                    <button
                        onClick={onToggle}
                        className={`btn btn-xs ${
                            hidden ? "btn-success" : "btn-error"
                        }`}
                    >
                        {hidden ? "unhide" : "hide"}
                    </button>
                </div>
            )}
        </div>
    );
}

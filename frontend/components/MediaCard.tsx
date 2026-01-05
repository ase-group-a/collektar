"use client";

import { HeartIcon as OutlineHeartIcon } from "@heroicons/react/24/outline";
import { HeartIcon as SolidHeartIcon } from "@heroicons/react/24/solid";
import { useState } from "react";
import { MediaItem } from "@/types/media";
import { useAuth } from "@/lib/auth/AuthProvider";
import {addItemToCollection, removeItemFromCollection} from "@/lib/api/collectionApi";

interface MediaCardProps {
    item: MediaItem & { addedToCollection?: boolean };
    layout?: "grid" | "list";
}

const MediaCard = ({ item, layout = "grid" }: MediaCardProps) => {
    const { isAuthenticated } = useAuth();
    const [liked, setLiked] = useState(item.addedToCollection ?? false);
    const [loading, setLoading] = useState(false);

    const handleLike = async () => {
        if (!isAuthenticated || loading) return;
        setLoading(true);

        try {
            if (liked) {
                await removeItemFromCollection(item.type, item.id);
                setLiked(false);
            } else {
                await addItemToCollection(
                    item.type,
                    item.id,
                    item.title,
                    item.image_url,
                    item.description,
                    item.source
                );
                setLiked(true);
            }
        } catch (err) {
            console.error("Failed to toggle collection", err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="card bg-base-200 w-40 border border-transparent hover:border-primary hover:shadow-md transition-all duration-200 flex flex-col relative">
            {isAuthenticated && (
                <button
                    className="absolute top-1 right-1 p-1 rounded-full z-20"
                    onClick={handleLike}
                    disabled={loading}
                >
                    {liked ? (
                        <SolidHeartIcon className="w-6 h-6 text-red-500" />
                    ) : (
                        <OutlineHeartIcon className="w-6 h-6 text-gray-400 hover:text-red-500" />
                    )}
                </button>
            )}

            <div className="flex-1 relative overflow-hidden">
                <img
                    src={item.image_url ?? "/collektar-logo-c.png"}
                    alt=""
                    className="absolute inset-0 w-full h-full object-cover scale-110 blur opacity-40"
                />

                <div className="relative z-10 flex items-center justify-center h-full">
                    <img
                        src={item.image_url ?? "/collektar-logo-c.png"}
                        alt={item.title}
                        className="max-h-full max-w-full object-contain"
                    />
                </div>
            </div>


            <h2 className="text-xs text-center truncate p-2">
                {item.title}
            </h2>
        </div>
    );
};

export default MediaCard;

import MediaCard from "./MediaCard";
import { MediaItem } from "@/types/media";
import {CollectionItemInfo} from "@/types/collection";

interface MediaListProps {
    items: MediaItem[];
    userItems?: CollectionItemInfo[];
    layout?: "grid" | "list";
}

const MediaList = ({ items, userItems, layout = "grid" }: MediaListProps) => {
    return (
        <div
            className={
                layout === "grid"
                    ? "grid gap-6 justify-items-center grid-cols-[repeat(auto-fit,minmax(180px,1fr))]"
                    : "flex flex-col gap-4"
            }
        >
            {items.map((item) => {
                const addedToCollection = userItems?.some(
                    (userItem) => userItem.item_id === item.id
                );
                return (
                    <MediaCard
                        key={item.id}
                        item={{ ...item, addedToCollection }}
                        layout={layout}
                    />
                );
            })}
        </div>
    );
};

export default MediaList;

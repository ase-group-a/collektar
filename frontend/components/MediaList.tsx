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
                const userItem = userItems?.find(
                    (ui) => ui.item_id === item.id || ui.item_id === null
                );

                const addedToCollection = !!userItem;

                return (
                    <MediaCard
                        key={userItem?.id ?? item.id}
                        item={{ ...item, addedToCollection }}
                        layout={layout}
                    />
                );
            })}

        </div>
    );
};

export default MediaList;

import MediaCard from "./MediaCard";
import { MediaItem } from "@/types/media";

interface MediaListProps {
    items: MediaItem[];
    layout?: "grid" | "list";
}

const MediaList = ({ items, layout = "grid" }: MediaListProps) => {
    return (
        <div
            className={
                layout === "grid"
                    ? "grid gap-6 justify-items-center grid-cols-[repeat(auto-fit,minmax(160px,1fr))]"
                    : "flex flex-col gap-4"
            }
        >
            {items.map((item) => (
                <MediaCard key={item.id} title={item.title} image={item.imageUrl || ""} layout={layout} />
            ))}
        </div>
    );
};


export default MediaList;

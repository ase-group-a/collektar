import MediaCard from "./MediaCard";
import {MediaItem} from "@/types/media";

interface MediaListProps {
    items: MediaItem[];
}

const MediaList = ({ items }: MediaListProps) => (
    <div className="grid gap-6 grid-cols-[repeat(auto-fit,minmax(160px,1fr))] justify-items-center items-start">
        {items.map((item) => (
            <MediaCard key={item.id} title={item.title} image={item.imageUrl || ""} />
        ))}
    </div>
);

export default MediaList;

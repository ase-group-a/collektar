import MediaCard from "./MediaCard";

interface MediaListProps {
    items: { id: string; title: string; image: string }[];
}

const MediaList = ({ items }: MediaListProps) => (
    <div className="grid gap-6 grid-cols-[repeat(auto-fit,minmax(160px,1fr))] justify-items-center items-start">
        {items.map((item) => (
            <MediaCard key={item.id} title={item.title} image={item.image} />
        ))}
    </div>
);

export default MediaList;

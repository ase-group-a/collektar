interface MediaCardProps {
    title: string;
    image: string;
    layout?: "grid" | "list";
}

const MediaCard = ({ title, image, layout = "grid" }: MediaCardProps) => {
    if (layout === "list") {
        return (
            <div className="card card-side bg-base-200 w-full border border-transparent hover:border-primary hover:shadow-md transition-all duration-200 items-center">
                <img src={image} alt={title} className="w-40 h-40 object-cover rounded" />
                <div className="ml-4 truncate">{title}</div>
            </div>
        );
    }

    return (
        <div className="card bg-base-200 w-40 border border-transparent hover:border-primary hover:shadow-md transition-all duration-200">
            <figure>
                <img src={image} alt={title} className="w-full h-auto object-contain" />
            </figure>
            <h2 className="text-xs text-center truncate p-2">{title}</h2>
        </div>
    );
};

export default MediaCard;

interface MediaCardProps {
    title: string;
    image: string;
}

const MediaCard = ({ title, image }: MediaCardProps) => (
    <div className="card bg-base-200 w-40 border border-transparent hover:border-primary hover:shadow-md transition-all duration-200">
        <figure>
            <img
                src={image}
                alt={title}
                className="w-full h-auto object-contain"
            />
        </figure>
        <h2 className="text-xs text-center truncate p-2">{title}</h2>
    </div>
);

export default MediaCard;

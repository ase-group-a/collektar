"use client";
import { useParams } from 'next/navigation';
import MediaList from '../../../components/MediaList';

const MediaPage = () => {
    const params = useParams();
    const { type } = params; // games, films, music, books

    // TODO: Placeholder later fetch from API
    const mediaItems = [
        { id: '1', title: `Minecraft`, image: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co8fu7.webp' },
        { id: '2', title: `Fortnite`, image: 'https://images.igdb.com/igdb/image/upload/t_cover_big/coa8yi.webp' },
        { id: '3', title: `Star Wars: The Force Awakens`, image: 'https://image.tmdb.org/t/p/w600_and_h900_bestv2/wqnLdwVXoBjKibFRR5U3y0aDUhs.jpg' },
        { id: '4', title: `True`, image: 'https://geo-media.beatport.com/image_size/1400x1400/7f749e2f-57bc-4d55-ba8d-0e1b6ec9170b.jpg' },
        { id: '5', title: `Minecraft`, image: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co8fu7.webp' },
        { id: '6', title: `Fortnite`, image: 'https://images.igdb.com/igdb/image/upload/t_cover_big/coa8yi.webp' },
        { id: '7', title: `Star Wars: The Force Awakens`, image: 'https://image.tmdb.org/t/p/w600_and_h900_bestv2/wqnLdwVXoBjKibFRR5U3y0aDUhs.jpg' },
        { id: '8', title: `True`, image: 'https://geo-media.beatport.com/image_size/1400x1400/7f749e2f-57bc-4d55-ba8d-0e1b6ec9170b.jpg' },
        { id: '9', title: `Minecraft`, image: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co8fu7.webp' },
        { id: '10', title: `Fortnite`, image: 'https://images.igdb.com/igdb/image/upload/t_cover_big/coa8yi.webp' },
        { id: '11', title: `Star Wars: The Force Awakens`, image: 'https://image.tmdb.org/t/p/w600_and_h900_bestv2/wqnLdwVXoBjKibFRR5U3y0aDUhs.jpg' },
        { id: '12', title: `True`, image: 'https://geo-media.beatport.com/image_size/1400x1400/7f749e2f-57bc-4d55-ba8d-0e1b6ec9170b.jpg' },
        { id: '13', title: `Minecraft`, image: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co8fu7.webp' },
        { id: '14', title: `Fortnite`, image: 'https://images.igdb.com/igdb/image/upload/t_cover_big/coa8yi.webp' },
        { id: '15', title: `Star Wars: The Force Awakens`, image: 'https://image.tmdb.org/t/p/w600_and_h900_bestv2/wqnLdwVXoBjKibFRR5U3y0aDUhs.jpg' },
        { id: '16', title: `True`, image: 'https://geo-media.beatport.com/image_size/1400x1400/7f749e2f-57bc-4d55-ba8d-0e1b6ec9170b.jpg' },
    ];

    return (
        <div className="px-80 py-10">
            <MediaList items={mediaItems} />
        </div>
    );
};

export default MediaPage;

"use client";
import { useParams } from 'next/navigation';
import useSWR from "swr";
import MediaList from '../../../components/MediaList';
import {MediaItem} from "@/types/media";
import {searchMedia} from "@/lib/api/mediaApi";

const MediaPage = () => {
    const params = useParams();
    const { type } = params;

    const query = "ncs";
    const limit = 40;
    const offset = 0;

    const { data, error, isLoading } = useSWR(
        [type, query, limit, offset],
        () => searchMedia(type as any, query, limit, offset)
    );

    if (isLoading) return <div>Loading...</div>;
    if (error) return <div>Error loading media: {(error as any).message}</div>;

    const mediaItems: MediaItem[] = data?.items || [];

    return (
        <div className="px-80 py-10">
            <MediaList items={mediaItems} />
        </div>
    );
};

export default MediaPage;

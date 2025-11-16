"use client";
import { useParams } from "next/navigation";
import useSWR from "swr";
import { useState } from "react";

import MediaList from "@/components/MediaList";
import { MediaItem } from "@/types/media";
import { searchMedia } from "@/lib/api/mediaApi";
import LoadingState from "@/components/common/LoadingState";
import ErrorState from "@/components/common/ErrorState";
import Pagination from "@/components/common/Pagination";
import MediaToolbar from "@/components/common/MediaToolbar";

const MediaPage = () => {
    const params = useParams();
    const { type } = params;

    const [viewMode, setViewMode] = useState<"grid" | "list">("grid");

    const [query, setQuery] = useState("");
    const [page, setPage] = useState(1);
    const limit = 28;
    const offset = (page - 1) * limit;

    const { data, error, isLoading } = useSWR(
        [type, query, limit, offset],
        () => searchMedia(type as any, query, limit, offset)
    );

    const mediaItems: MediaItem[] = data?.items || [];
    const totalPages = Math.ceil((data?.total || 0) / limit);

    return (
        <div className="px-80 py-10 text-center">
            <MediaToolbar
                query={query}
                setQuery={setQuery}
                viewMode={viewMode}
                setViewMode={setViewMode}
                resetPage={() => setPage(1)}
            />

            {isLoading ? (
                <LoadingState />
            ) : error ? (
                <ErrorState message={(error as any).message} onRetry={() => window.location.reload()}/>
            ) : (
                <MediaList items={mediaItems} layout={viewMode}/>
            )}

            {totalPages > 1 && (
                    <Pagination
                        page={page}
                        totalPages={totalPages}
                        onPageChange={(p) => setPage(p)}
                    />
            )}
        </div>
    );
};

export default MediaPage;

"use client";
import { useParams } from "next/navigation";
import useSWR from "swr";
import { useState } from "react";

import MediaList from "@/components/MediaList";
import { MediaItem } from "@/types/media";
import { searchMedia } from "@/lib/api/mediaApi";
import LoadingState from "@/components/common/LoadingState";
import ErrorState from "@/components/common/ErrorState";
import SearchBar from "@/components/common/SearchBar";
import Pagination from "@/components/common/Pagination";

const MediaPage = () => {
    const params = useParams();
    const { type } = params;

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
        <div className="px-80 py-10">
            <div className="pb-5">
                <SearchBar
                    initialQuery={query}
                    onSearch={(q) => {
                        setQuery(q);
                        setPage(1);
                    }}
                />
            </div>

            {isLoading ? (
                <div className="text-center">
                    <LoadingState />
                </div>
            ) : error ? (
                <div className="text-center">
                    <ErrorState
                        message={(error as any).message}
                        onRetry={() => window.location.reload()}
                    />
                </div>
            ) : (
                <MediaList items={mediaItems} />
            )}

            {totalPages > 1 && (
                <div className="text-center">
                    <Pagination
                        page={page}
                        totalPages={totalPages}
                        onPageChange={(p) => setPage(p)}
                    />
                </div>
            )}
        </div>
    );
};

export default MediaPage;

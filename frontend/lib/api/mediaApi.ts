import { fetcher } from "../fetcher";
import { MediaItem, SearchResult } from "@/types/media";

export const searchMedia = async (
    type: "games" | "movies" | "shows" | "music" | "books" | "board game",
    query: string,
    limit = 10,
    offset = 0
): Promise<SearchResult<MediaItem>> => {
    const path = `/api/media/${type}?q=${encodeURIComponent(query)}&limit=${limit}&offset=${offset}`;
    return fetcher<SearchResult<MediaItem>>(path);
};

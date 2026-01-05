export type MediaType = "GAME" | "MOVIE" | "SHOW" | "MUSIC" | "BOOK" | "BOARD GAME";

export interface MediaItem {
    id: string;
    title: string;
    type: MediaType | string;
    image_url?: string | null;
    description?: string | null;
    source?: string | null;
}

export interface SearchResult<T> {
    total: number;
    limit: number;
    offset: number;
    items: T[];
}
export type MediaType = "GAME" | "MOVIE" | "SHOW" | "MUSIC" | "BOOK";

export interface MediaItem {
    id: string;
    item_id?: string | null;
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
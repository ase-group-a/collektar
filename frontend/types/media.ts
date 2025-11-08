export type MediaType = "GAME" | "MOVIE" | "SHOW" | "MUSIC" | "BOOK";

export interface MediaItem {
    id: string;
    title: string;
    type: MediaType | string;
    imageUrl?: string | null;
    description?: string | null;
    source?: string | null;
}

export interface SearchResult<T> {
    total: number;
    limit: number;
    offset: number;
    items: T[];
}
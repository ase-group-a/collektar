export interface CollectionInfo {
    id: string;
    type: string;
    hidden: boolean;
    createdAt: number;
}

export interface CollectionItemInfo {
    id: string;
    item_id: string;
    title?: string | null;
    image_url?: string | null;
    description?: string | null;
    source?: string | null;
    added_at: number;
}

export const DEFAULT_COLLECTION_TYPES = [
    "GAMES",
    "MOVIES",
    "SHOWS",
    "BOOKS",
    "MUSIC",
    "BOARDGAMES",
] as const;

export const isDefaultCollection = (type: string) =>
    DEFAULT_COLLECTION_TYPES.includes(type.toUpperCase() as any);

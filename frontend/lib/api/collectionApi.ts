import { fetcher } from "../fetcher";
import { CollectionInfo, CollectionItemInfo } from "@/types/collection";

export const getCollections = async (): Promise<CollectionInfo[]> => {
    return fetcher<CollectionInfo[]>("/api/collection/collections");
};

export const getCollectionItems = async (
    type: string
): Promise<CollectionItemInfo[]> => {
    const res = await fetcher<CollectionItemInfo[]>(
        `/api/collection/collections/${type}/items`
);
    return res;
};


export const addItemToCollection = async (
    type: string,
    item_id: string,
    title?: string,
    image_url?: string | null | undefined,
    description?: string | null | undefined,
    source?: string | null | undefined
): Promise<CollectionItemInfo> => {
    return fetcher<CollectionItemInfo>(
        `/api/collection/collections/${type.toLowerCase()}/items`,
        {
            method: "POST",
            body: JSON.stringify({ item_id, title, image_url, description, source }),
        }
    );
};

export const removeItemFromCollection = async (type: string, itemId: string): Promise<void> => {
    return fetcher(
        `/api/collection/collections/${type.toLowerCase()}/items/${itemId}`,
        { method: "DELETE" }
    );
};

export const createCollection = async (type: string): Promise<CollectionInfo> => {
    return fetcher<CollectionInfo>("/api/collection/collections", {
        method: "POST",
        body: JSON.stringify({ type }),
    });
};

export const deleteCollection = async (id: string): Promise<void> => {
    return fetcher(`/api/collection/collections/${id}`, {
        method: "DELETE",
    });
};
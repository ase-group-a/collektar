"use client";

import { useEffect, useState } from "react";
import { getCollectionItems, addItemToCollection } from "@/lib/api/collectionApi";
import { useParams } from "next/navigation";
import MediaList from "@/components/MediaList";
import { CollectionItemInfo } from "@/types/collection";
import { MediaItem } from "@/types/media";
import Link from "next/link";

export default function CollectionPage() {
    const params = useParams();
    const collection = params.collection as string;
    const username = params.username as string;

    const [items, setItems] = useState<CollectionItemInfo[]>([]);
    const [loading, setLoading] = useState(true);

    const [showAdd, setShowAdd] = useState(false);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [form, setForm] = useState({
        itemId: "",
        title: "",
        image_url: "",
        description: "",
        source: "",
    });

    useEffect(() => {
        let mounted = true;
        getCollectionItems(collection)
            .then(data => { if (mounted) setItems(data ?? []); })
            .finally(() => { if (mounted) setLoading(false); });
        return () => { mounted = false; };
    }, [collection]);

    const mappedItems: MediaItem[] = items.map(item => ({
        id: item.item_id,
        title: item.title ?? "",
        image_url: item.image_url ?? null,
        description: item.description ?? null,
        type: collection,
        source: item.source ?? null,
    }));

    const handleAddItem = async () => {
        if (!form.title) {
            setError("Title is required");
            return;
        }

        setSaving(true);
        setError(null);

        try {
            const created = await addItemToCollection(
                collection,
                form.itemId.trim(),
                form.title || undefined,
                form.image_url || undefined,
                form.description || undefined,
                form.source || undefined
            );

            setItems(prev => [...prev, {
                id: created.id,
                item_id: collection,
                title: form.title || null,
                image_url: form.image_url || null,
                description: form.description || null,
                source: form.source || null,
                added_at: Date.now(),
            }]);

            setShowAdd(false);
            setForm({ itemId: "", title: "", image_url: "", description: "", source: "" });
        } catch (e: any) {
            setError(e?.message ?? "Failed to add item");
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="max-w-6xl mx-auto p-6">
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <Link
                        href={`/u/${username}`}
                        className="btn btn-ghost btn-sm"
                    >
                        ←
                    </Link>

                    <h1 className="text-2xl font-bold capitalize">
                        {collection}
                    </h1>
                </div>

                <button
                    className="btn btn-sm"
                    onClick={() => setShowAdd(true)}
                >
                    + Add Item
                </button>
            </div>

            {loading ? (
                <div className="loading loading-spinner" />
            ) : (
                <MediaList items={mappedItems} userItems={items} layout="grid" />
            )}

            {showAdd && (
                <div className="modal modal-open">
                    <div className="modal-box">
                        <h3 className="font-bold text-lg mb-4">
                            Add Item to {collection}
                        </h3>

                        <div className="space-y-3">
                            <input
                                className="input input-bordered w-full"
                                placeholder="Title"
                                value={form.title}
                                onChange={e => setForm({ ...form, title: e.target.value })}
                            />
                            <input
                                className="input input-bordered w-full"
                                placeholder="Image URL"
                                value={form.image_url}
                                onChange={e => setForm({ ...form, image_url: e.target.value })}
                            />
                            <textarea
                                className="textarea textarea-bordered w-full"
                                placeholder="Description"
                                value={form.description}
                                onChange={e => setForm({ ...form, description: e.target.value })}
                            />
                            <input
                                className="input input-bordered w-full"
                                placeholder="Source"
                                value={form.source}
                                onChange={e => setForm({ ...form, source: e.target.value })}
                            />
                            {error && <p className="text-sm text-error">{error}</p>}
                        </div>

                        <div className="modal-action">
                            <button
                                className="btn"
                                onClick={() => setShowAdd(false)}
                                disabled={saving}
                            >
                                Cancel
                            </button>
                            <button
                                className="btn"
                                onClick={handleAddItem}
                                disabled={saving}
                            >
                                {saving ? "Saving..." : "Add"}
                            </button>

                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

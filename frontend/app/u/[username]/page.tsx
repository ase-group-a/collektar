"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/lib/auth/AuthProvider";
import { getCollections, createCollection, deleteCollection } from "@/lib/api/collectionApi";
import { CollectionInfo } from "@/types/collection";
import Link from "next/link";
import {isDefaultCollection} from "@/types/collection";

export default function UserPage() {
    const { user } = useAuth();
    const [collections, setCollections] = useState<CollectionInfo[]>([]);
    const [editMode, setEditMode] = useState(false);
    const [loading, setLoading] = useState(true);

    const [adding, setAdding] = useState(false);
    const [newType, setNewType] = useState("");
    const [addError, setAddError] = useState<string | null>(null);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        let mounted = true;
        const load = async () => {
            try {
                const cols = await getCollections();
                if (mounted) setCollections(cols);
            } catch (err) {
                console.error("Failed to load collections", err);
            } finally {
                if (mounted) setLoading(false);
            }
        };
        load();
        return () => { mounted = false; };
    }, []);

    const visibleCollections = editMode ? collections : collections.filter(c => !c.hidden);

    const handleAddClick = () => {
        setAddError(null);
        setNewType("");
        setAdding(true);
    };

    const handleCancelAdd = () => {
        setAdding(false);
        setNewType("");
        setAddError(null);
    };

    const handleSaveAdd = async () => {
        setAddError(null);
        const typeTrimmed = newType.trim();
        if (!typeTrimmed) {
            setAddError("Choose a name.");
            return;
        }

        if (collections.some(c => c.type.toLowerCase() === typeTrimmed.toLowerCase())) {
            setAddError("Collection already exists.");
            return;
        }

        setSaving(true);
        try {
            const created = await createCollection(typeTrimmed);
            const cols = await getCollections();
            setCollections(cols);
            setAdding(false);
            setNewType("");
        } catch (err: any) {
            console.error("Create collection failed", err);
            setAddError(err?.message ?? "Error creating Collection");
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id: string) => {
        if (!confirm("Do you want to delete this collection?")) return;

        try {
            await deleteCollection(id);
            setCollections(prev => prev.filter(c => c.id !== id));
        } catch (err) {
            console.error("Delete failed", err);
            alert("Deletion failed.");
        }
    };

    return (
        <div className="max-w-5xl mx-auto p-6">
            <div className="flex justify-between items-start mb-8">
                <div>
                    <h1 className="text-3xl font-bold">
                        {user?.display_name}
                    </h1>
                    <p className="text-sm text-base-content/60">
                        @{user?.username}
                    </p>
                </div>

                <button
                    className="btn btn-outline btn-sm"
                    onClick={() => setEditMode(!editMode)}
                >
                    {editMode ? "Done" : "Edit"}
                </button>
            </div>

            <h2 className="text-xl font-semibold mb-4">Collections</h2>

            {loading ? (
                <div className="py-8 text-center">Loading...</div>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
                    {visibleCollections.map(collection => {
                        const slug = collection.type.toLowerCase();
                        const cardClasses = collection.hidden ? "opacity-40" : "";
                        const isDefault = isDefaultCollection(collection.type);

                        return (
                            <div
                                key={collection.id}
                                className={`card bg-base-200 hover:bg-base-300 transition ${cardClasses} relative`}
                            >
                                <Link
                                    href={`/u/${user?.username}/${slug}`}
                                    className="card-body block"
                                >
                                    <h3 className="card-title capitalize">{slug}</h3>

                                    {collection.hidden && editMode && (
                                        <span className="badge badge-outline badge-sm">
                        hidden
                    </span>
                                    )}
                                </Link>

                                {editMode && (
                                    <div className="p-2 absolute top-2 right-2">
                                        {isDefault ? (
                                            <span className="badge badge-primary badge-sm">
                            default
                        </span>
                                        ) : (
                                            <button
                                                className="btn btn-sm btn-error btn-outline"
                                                onClick={() => handleDelete(collection.id)}
                                                title="Delete collection"
                                            >
                                                Delete
                                            </button>
                                        )}
                                    </div>
                                )}
                            </div>
                        );
                    })}
                </div>
            )}

            <div className="mt-4">
                {!adding ? (
                    <div className="text-left">
                        <button className="btn btn-ghost btn-xs" onClick={handleAddClick}>
                            + Add Collection
                        </button>
                    </div>
                ) : (
                    <div className="card-body">
                        <label className="block mb-2 text-sm font-medium">Collection Name</label>
                        <input
                            type="text"
                            className="input input-bordered w-70"
                            value={newType}
                            onChange={(e) => setNewType(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === "Enter") handleSaveAdd();
                                if (e.key === "Escape") handleCancelAdd();
                            }}
                        />
                        {addError && <p className="text-sm text-error">{addError}</p>}

                        <div className="flex gap-2 mt-2">
                            <button className={`btn btn-primary btn-sm ${saving ? "loading" : ""}`} onClick={handleSaveAdd} disabled={saving}>
                                Save
                            </button>
                            <button className="btn btn-ghost btn-sm" onClick={handleCancelAdd} disabled={saving}>
                                Cancel
                            </button>
                        </div>
                    </div>
                )}
            </div>

        </div>
    );
}

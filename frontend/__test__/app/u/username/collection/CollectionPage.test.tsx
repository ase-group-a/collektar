import React from "react";
import { render, screen, waitFor, fireEvent, act } from "@testing-library/react";
import "@testing-library/jest-dom";
import CollectionPage from "@/app/u/[username]/[collection]/page";

jest.mock("@/lib/api/collectionApi", () => ({
    getCollectionItems: jest.fn(),
    addItemToCollection: jest.fn(),
}));

jest.mock("next/navigation", () => ({
    useParams: jest.fn(),
}));

jest.mock("@/components/MediaList", () => {
    return {
        __esModule: true,
        default: (props: any) => {
            return <div data-testid="media-list" data-items={JSON.stringify(props.items)} />;
        },
    };
});

import { getCollectionItems, addItemToCollection } from "@/lib/api/collectionApi";
import { useParams } from "next/navigation";

describe("CollectionPage", () => {
    beforeEach(() => {
        jest.resetAllMocks();
        (useParams as jest.Mock).mockReturnValue({
            collection: "movies",
            username: "alice",
        });
    });

    it("renders MediaList after loading items", async () => {
        const items = [
            {
                id: "1",
                item_id: "tt123",
                title: "My Movie",
                image_url: "http://image",
                description: "desc",
                source: "imdb",
                added_at: Date.now(),
            },
        ];
        (getCollectionItems as jest.Mock).mockResolvedValue(items);

        render(<CollectionPage />);

        await waitFor(() => expect(screen.getByTestId("media-list")).toBeInTheDocument());

        const mediaList = screen.getByTestId("media-list");
        expect(mediaList).toHaveAttribute("data-items", JSON.stringify(
            items.map(it => ({
                id: it.item_id,
                title: it.title ?? "",
                image_url: it.image_url ?? null,
                description: it.description ?? null,
                type: "movies",
                source: it.source ?? null,
            }))
        ));
    });

    it("opens add modal and shows validation error when title is missing", async () => {
        (getCollectionItems as jest.Mock).mockResolvedValue([]);
        render(<CollectionPage />);

        await waitFor(() => expect(screen.getByTestId("media-list")).toBeInTheDocument());

        const addButton = screen.getByRole("button", { name: "+ Add Item" });
        fireEvent.click(addButton);

        const modalAddBtn = await screen.findByRole("button", { name: "Add" });
        fireEvent.click(modalAddBtn);

        expect(await screen.findByText("Title is required")).toBeInTheDocument();

        expect(addItemToCollection).not.toHaveBeenCalled();
    });
});

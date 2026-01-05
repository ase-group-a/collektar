jest.mock("@/lib/api/collectionApi", () => ({
    getCollections: jest.fn(),
    createCollection: jest.fn(),
    deleteCollection: jest.fn(),
}));

jest.mock("@/lib/auth/AuthProvider", () => ({
    useAuth: jest.fn().mockReturnValue({
        user: { username: "tester", display_name: "Test User" },
    }),
}));

jest.mock("next/link", () => ({
    __esModule: true,
    default: ({ href, children }: any) => <a href={href}>{children}</a>,
}));

import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";

import UserPage from "@/app/u/[username]/page";
import {
    getCollections,
    createCollection,
    deleteCollection,
} from "@/lib/api/collectionApi";

const mockedGetCollections = getCollections as unknown as jest.Mock;
const mockedCreateCollection = createCollection as unknown as jest.Mock;
const mockedDeleteCollection = deleteCollection as unknown as jest.Mock;

describe("UserPage", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    const collections = [
        { id: "1", type: "music", hidden: false },
        { id: "2", type: "movies", hidden: true },
    ];

    it("loads and displays user collections (hides hidden ones by default)", async () => {
        mockedGetCollections.mockResolvedValueOnce(collections);

        render(<UserPage />);

        expect(screen.getByText("Loading...")).toBeInTheDocument();

        await screen.findByText("music");

        expect(screen.queryByText("movies")).not.toBeInTheDocument();
    });

    it("shows hidden collections when edit mode is toggled", async () => {
        mockedGetCollections.mockResolvedValueOnce(collections);

        render(<UserPage />);

        await screen.findByText("music");

        fireEvent.click(screen.getByText("Edit"));

        expect(screen.getByText("movies")).toBeInTheDocument();
        expect(screen.getByText("hidden")).toBeInTheDocument();
    });

    it("can add a new collection (calls createCollection and refreshes list)", async () => {
        mockedGetCollections
            .mockResolvedValueOnce(collections) // initial load
            .mockResolvedValueOnce([
                ...collections,
                { id: "3", type: "books", hidden: false },
            ]);

        mockedCreateCollection.mockResolvedValueOnce({ id: "3", type: "books" });

        render(<UserPage />);

        await screen.findByText("music");

        fireEvent.click(screen.getByText("+ Add Collection"));

        const input = screen.getByRole("textbox");
        fireEvent.change(input, { target: { value: "books" } });

        fireEvent.click(screen.getByText("Save"));

        await screen.findByText("books");

        expect(mockedCreateCollection).toHaveBeenCalledWith("books");
    });

    it("prevents adding duplicate collection (shows error)", async () => {
        mockedGetCollections.mockResolvedValueOnce(collections);

        render(<UserPage />);

        await screen.findByText("music");

        fireEvent.click(screen.getByText("+ Add Collection"));

        const input = screen.getByRole("textbox");
        fireEvent.change(input, { target: { value: "music" } });

        fireEvent.click(screen.getByText("Save"));

        expect(
            await screen.findByText("Collection already exists.")
        ).toBeInTheDocument();

        expect(mockedCreateCollection).not.toHaveBeenCalled();
    });

    it("deletes a non-default collection after confirmation", async () => {
        mockedGetCollections.mockResolvedValueOnce([
            { id: "1", type: "music", hidden: false },
            { id: "2", type: "custom", hidden: false },
        ]);

        mockedDeleteCollection.mockResolvedValueOnce(undefined);
        jest.spyOn(window, "confirm").mockReturnValueOnce(true);

        render(<UserPage />);

        await screen.findByText("music");

        fireEvent.click(screen.getByText("Edit"));

        fireEvent.click(
            screen.getByRole("button", { name: /delete/i })
        );

        await waitFor(() => {
            expect(mockedDeleteCollection).toHaveBeenCalledWith("2");
        });

        await waitFor(() => {
            expect(screen.queryByText("custom")).not.toBeInTheDocument();
        });
    });


});
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import MediaCard from "@/components/MediaCard";

jest.mock("@/lib/auth/AuthProvider", () => ({
    useAuth: jest.fn(),
}));
jest.mock("@/lib/api/collectionApi", () => ({
    addItemToCollection: jest.fn(),
    removeItemFromCollection: jest.fn(),
}));

import { useAuth as mockedUseAuth } from "@/lib/auth/AuthProvider";
import {
    addItemToCollection as mockedAddItem,
    removeItemFromCollection as mockedRemoveItem,
} from "@/lib/api/collectionApi";

describe("MediaCard", () => {
    const baseItem = {
        id: "1",
        type: "movie",
        title: "Test Media",
        image_url: "/test.jpg",
        description: "desc",
        source: "src",
    } as const;

    beforeEach(() => {
        jest.clearAllMocks();
        (mockedUseAuth as jest.Mock).mockReturnValue({ isAuthenticated: false });
    });

    it("renders title, image and card structure", () => {
        render(<MediaCard item={{ ...baseItem, addedToCollection: false }} layout="grid" />);

        expect(screen.getByText(baseItem.title)).toBeInTheDocument();
        const img = screen.getByAltText(baseItem.title) as HTMLImageElement;
        expect(img).toBeInTheDocument();
        expect(img.src).toContain(baseItem.image_url); // JSDOM reports absolute URL, so use contains

        const card = screen.getByText(baseItem.title).closest(".card");
        expect(card).toBeTruthy();
        expect(card).toHaveClass("w-40");
    });

    it("does not show like button when not authenticated", () => {
        (mockedUseAuth as jest.Mock).mockReturnValue({ isAuthenticated: false });
        render(<MediaCard item={{ ...baseItem, addedToCollection: false }} />);

        expect(screen.queryByRole("button")).toBeNull();
    });

    it("shows like button when authenticated and toggles to liked after clicking (calls addItemToCollection)", async () => {
        (mockedUseAuth as jest.Mock).mockReturnValue({ isAuthenticated: true });
        (mockedAddItem as jest.Mock).mockResolvedValueOnce(undefined);

        render(<MediaCard item={{ ...baseItem, addedToCollection: false }} />);

        const btn = screen.getByRole("button") as HTMLButtonElement;
        expect(btn).toBeInTheDocument();

        const svgBefore = btn.querySelector("svg");
        expect(svgBefore).toBeTruthy();
        expect(svgBefore?.classList.contains("text-gray-400")).toBe(true);

        fireEvent.click(btn);

        await waitFor(() => {
            expect(mockedAddItem).toHaveBeenCalledWith(
                baseItem.type,
                baseItem.id,
                baseItem.title,
                baseItem.image_url,
                baseItem.description,
                baseItem.source
            );
        });

        const svgAfter = btn.querySelector("svg");
        expect(svgAfter).toBeTruthy();
        expect(svgAfter?.classList.contains("text-red-500")).toBe(true);
    });

    it("clicking when initially liked calls removeItemFromCollection and switches to not liked", async () => {
        (mockedUseAuth as jest.Mock).mockReturnValue({ isAuthenticated: true });
        (mockedRemoveItem as jest.Mock).mockResolvedValueOnce(undefined);

        render(<MediaCard item={{ ...baseItem, addedToCollection: true }} />);

        const btn = screen.getByRole("button") as HTMLButtonElement;
        expect(btn).toBeInTheDocument();

        const svgBefore = btn.querySelector("svg");
        expect(svgBefore).toBeTruthy();
        expect(svgBefore?.classList.contains("text-red-500")).toBe(true);

        fireEvent.click(btn);

        await waitFor(() => {
            expect(mockedRemoveItem).toHaveBeenCalledWith(baseItem.type, baseItem.id);
        });

        const svgAfter = btn.querySelector("svg");
        expect(svgAfter).toBeTruthy();
        expect(svgAfter?.classList.contains("text-gray-400")).toBe(true);
    });

    it("disables the like button while the API call is in progress", async () => {
        (mockedUseAuth as jest.Mock).mockReturnValue({ isAuthenticated: true });

        let resolvePromise!: () => void;
        const inFlight = new Promise<void>((res) => { resolvePromise = res; });
        (mockedAddItem as jest.Mock).mockReturnValueOnce(inFlight);

        render(<MediaCard item={{ ...baseItem, addedToCollection: false }} />);

        const btn = screen.getByRole("button") as HTMLButtonElement;
        expect(btn).not.toBeDisabled();

        fireEvent.click(btn);
        expect(btn).toBeDisabled();

        resolvePromise();

        await waitFor(() => {
            expect(btn).not.toBeDisabled();
        });
    });


    it("uses fallback image when image_url is null/undefined", () => {
        render(<MediaCard item={{ ...baseItem, image_url: undefined, addedToCollection: false }} />);

        const img = screen.getByAltText(baseItem.title) as HTMLImageElement;
        expect(img.src).toContain("/collektar-logo-c.png");
    });
});

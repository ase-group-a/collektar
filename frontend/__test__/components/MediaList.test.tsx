import React from "react";
import { render, screen } from "@testing-library/react";

jest.mock("@/components/MediaCard", () => {
    const React = require("react");
    return {
        __esModule: true,
        default: ({ item, layout }: any) =>
            React.createElement(
                "div",
                {
                    className: "card",
                    "data-testid": `media-card-${item.id}`,
                    "data-layout": layout,
                },
                React.createElement("img", { alt: item.title, src: item.image_url }),
                React.createElement("div", null, item.title)
            ),
    };
});

import MediaList from "@/components/MediaList";
import { MediaItem } from "@/types/media";

describe("MediaList", () => {
    const items: MediaItem[] = [
        { id: "1", title: "Media 1", type: "MUSIC", image_url: "/1.jpg" } as MediaItem,
        { id: "2", title: "Media 2", type: "MUSIC", image_url: "/2.jpg" } as MediaItem,
    ];

    const renderList = (layout?: "grid" | "list") => {
        render(<MediaList items={items} layout={layout} />);
        const firstCard = screen.getByText(items[0].title).closest(".card");
        const listContainer = firstCard?.parentElement ?? null;
        return { listContainer };
    };

    beforeEach(() => {
        jest.clearAllMocks();
    });

    it("renders all media items in grid layout", () => {
        const { listContainer } = renderList("grid");

        items.forEach((item) => {
            expect(screen.getByText(item.title)).toBeInTheDocument();
            expect(screen.getByAltText(item.title)).toBeInTheDocument();
        });

        expect(listContainer).toHaveClass("grid");
    });

    it("renders all media items in list layout", () => {
        const { listContainer } = renderList("list");

        items.forEach((item) => {
            expect(screen.getByText(item.title)).toBeInTheDocument();
            expect(screen.getByAltText(item.title)).toBeInTheDocument();
        });

        expect(listContainer).toHaveClass("flex");
        expect(listContainer).toHaveClass("flex-col");
    });

    it("defaults to grid layout if layout not provided", () => {
        const { listContainer } = renderList();

        expect(listContainer).toHaveClass("grid");
    });

    it("marks items as addedToCollection when userItems contains item ids", () => {
        const userItems = [{ item_id: "2" }];
        render(<MediaList items={items} userItems={userItems as any} />);

        expect(screen.getByTestId("media-card-1")).toBeInTheDocument();
        expect(screen.getByTestId("media-card-2")).toBeInTheDocument();

        expect(screen.getByTestId("media-card-1")).toHaveAttribute("data-layout", "grid");
        expect(screen.getByTestId("media-card-2")).toHaveAttribute("data-layout", "grid");
    });
});

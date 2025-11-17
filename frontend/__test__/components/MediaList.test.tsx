import { render, screen } from "@testing-library/react";
import MediaList from "@/components/MediaList";
import { MediaItem } from "@/types/media";

describe("MediaList", () => {
    const items: MediaItem[] = [
        { id: "1", title: "Media 1", type: "MUSIC", imageUrl: "/1.jpg" },
        { id: "2", title: "Media 2", type: "MUSIC", imageUrl: "/2.jpg" },
    ];

    const renderList = (layout?: "grid" | "list") => {
        render(<MediaList items={items} layout={layout} />);
        const firstCard = screen.getByText(items[0].title).closest(".card");
        const listContainer = firstCard?.parentElement;
        return { listContainer };
    };

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
});

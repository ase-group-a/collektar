import { render, screen } from "@testing-library/react";
import MediaCard from "@/components/MediaCard";

describe("MediaCard", () => {
    const title = "Test Media";
    const image = "/test.jpg";

    it("renders grid layout correctly", () => {
        render(<MediaCard title={title} image={image} layout="grid" />);

        expect(screen.getByAltText(title)).toBeInTheDocument();
        expect(screen.getByText(title)).toBeInTheDocument();

        const card = screen.getByText(title).closest(".card");
        expect(card).toBeTruthy();
        expect(card).toHaveClass("w-40");
    });

    it("renders list layout correctly", () => {
        render(<MediaCard title={title} image={image} layout="list" />);

        expect(screen.getByAltText(title)).toBeInTheDocument();
        expect(screen.getByText(title)).toBeInTheDocument();

        const card = screen.getByText(title).closest(".card");
        expect(card).toBeTruthy();
        expect(card).toHaveClass("w-full");
    });

    it("defaults to grid layout if layout is not provided", () => {
        render(<MediaCard title={title} image={image} />);
        const card = screen.getByText(title).closest(".card");
        expect(card).toBeTruthy();
        expect(card).toHaveClass("w-40");
    });
});

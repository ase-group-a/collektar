import { render, screen } from "@testing-library/react";
import MediaCard from "@/components/MediaCard";

describe("MediaCard", () => {
    const title = "Test Media";
    const image = "/test.jpg";
    const missingImageUrl = "/image_missing.png";

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

    it("null image url is replaced with placeholder image", () => {
        render(<MediaCard title={title} image={null} />);
        const image = screen.getByAltText(title);
        expect(image).toHaveAttribute("src", missingImageUrl)
    });

    it("empty image url is replaced with placeholder image", () => {
        render(<MediaCard title={title} image={""} />);
        const image = screen.getByAltText(title);
        expect(image).toHaveAttribute("src", missingImageUrl)
    });
});

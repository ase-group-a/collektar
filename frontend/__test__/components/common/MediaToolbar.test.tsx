import { render, screen, fireEvent } from "@testing-library/react";
import MediaToolbar from "@/components/common/MediaToolbar";

describe("MediaToolbar", () => {
    const defaultProps = {
        query: "",
        setQuery: jest.fn(),
        viewMode: "grid" as "grid" | "list",
        setViewMode: jest.fn(),
        resetPage: jest.fn(),
    };

    it("renders SearchBar and buttons", () => {
        render(<MediaToolbar {...defaultProps} />);

        expect(screen.getByRole("searchbox")).toBeInTheDocument();

        expect(screen.getByLabelText("List View")).toBeInTheDocument();
        expect(screen.getByLabelText("Grid View")).toBeInTheDocument();
    });

    it("calls setQuery and resetPage on search", () => {
        render(<MediaToolbar {...defaultProps} />);
        const input = screen.getByRole("searchbox");

        fireEvent.change(input, { target: { value: "test" } });
        fireEvent.keyDown(input, { key: "Enter", code: "Enter" });

        expect(defaultProps.setQuery).toHaveBeenCalledWith("test");
        expect(defaultProps.resetPage).toHaveBeenCalled();
    });

    it("toggles view mode when buttons are clicked", () => {
        render(<MediaToolbar {...defaultProps} />);

        const listBtn = screen.getByLabelText("List View");
        const gridBtn = screen.getByLabelText("Grid View");

        fireEvent.click(listBtn);
        expect(defaultProps.setViewMode).toHaveBeenCalledWith("list");

        fireEvent.click(gridBtn);
        expect(defaultProps.setViewMode).toHaveBeenCalledWith("grid");
    });
});

import { render, screen, fireEvent } from "@testing-library/react";
import ErrorState from "@/components/common/ErrorState";

describe("ErrorState", () => {
    it("renders default error message", () => {
        render(<ErrorState />);
        expect(screen.getByText(/Failed to load media/i)).toBeInTheDocument();
        expect(screen.getByText(/unexpected error/i)).toBeInTheDocument();
    });

    it("renders custom message", () => {
        render(<ErrorState message="Something went wrong!" />);
        expect(screen.getByText("Something went wrong!")).toBeInTheDocument();
    });

    it("calls onRetry when button clicked", () => {
        const mockRetry = jest.fn();
        render(<ErrorState onRetry={mockRetry} />);
        fireEvent.click(screen.getByRole("button", { name: /retry/i }));
        expect(mockRetry).toHaveBeenCalledTimes(1);
    });
});

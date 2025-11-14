import { render, screen } from "@testing-library/react";
import LoadingState from "@/components/common/LoadingState";

describe("LoadingState", () => {
    it("renders the spinner", () => {
        render(<LoadingState />);
        const spinner = screen.getByRole("status");
        expect(spinner).toBeInTheDocument();
        expect(spinner).toHaveClass("loading-spinner");
    });
});

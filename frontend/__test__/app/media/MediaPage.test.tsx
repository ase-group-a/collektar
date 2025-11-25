import { render, screen, fireEvent } from "@testing-library/react";
import useSWR from "swr";
import MediaPage from "@/app/media/[type]/page";

jest.mock("swr");

jest.mock("next/navigation", () => ({
    useParams: jest.fn(),
}));
import { useParams } from "next/navigation";

jest.mock("@/components/MediaList", () => () => (
    <div data-testid="media-list">MediaListMock</div>
));

let capturedOnRetryProp: unknown = null;
let internalRetryMock = jest.fn();

jest.mock("@/components/common/ErrorState", () => {
    return ({ message, onRetry }: any) => {
        capturedOnRetryProp = onRetry;
        internalRetryMock = jest.fn();
        return (
            <div>
                <div>{message}</div>
                <button onClick={() => internalRetryMock()} aria-label="mock-retry">
                    Retry (mock)
                </button>
            </div>
        );
    };
});

describe("MediaPage", () => {
    const mockUseSWR = useSWR as jest.Mock;
    const mockUseParams = useParams as jest.Mock;

    beforeEach(() => {
        jest.clearAllMocks();
        capturedOnRetryProp = null;
        internalRetryMock = jest.fn();
        mockUseParams.mockReturnValue({ type: "movies" });
    });

    it("shows loading state", () => {
        mockUseSWR.mockReturnValue({ isLoading: true, data: undefined, error: undefined });
        render(<MediaPage />);

        const spinner = screen.getByRole("status");
        expect(spinner).toBeInTheDocument();
        expect(spinner).toHaveClass("loading-spinner");
    });

    it("shows error state and provides a retry prop (without calling real reload)", () => {
        const errorMessage = "Failed fetch";
        mockUseSWR.mockReturnValue({ isLoading: false, data: undefined, error: { message: errorMessage } });

        render(<MediaPage />);

        expect(screen.getByText(errorMessage)).toBeInTheDocument();

        expect(typeof capturedOnRetryProp).toBe("function");

        const retryButton = screen.getByRole("button", { name: /mock-retry/i });
        expect(retryButton).toBeInTheDocument();

        fireEvent.click(retryButton);
        expect(internalRetryMock).toHaveBeenCalledTimes(1);
    });

    it("renders media list when data is loaded", () => {
        mockUseSWR.mockReturnValue({
            isLoading: false,
            data: { items: [{ id: 1, title: "Test Item" }] },
            error: undefined,
        });

        render(<MediaPage />);
        expect(screen.getByTestId("media-list")).toBeInTheDocument();
    });
});

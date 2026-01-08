import React from "react";
import { render, screen, fireEvent, waitFor, act } from "@testing-library/react";
import ForgotPasswordPage from "@/app/forgot-password/page";
import { authApi } from "@/lib/api/authApi";

jest.mock("@/lib/api/authApi", () => ({
    authApi: {
        forgotPassword: jest.fn(),
    },
}));

jest.mock("next/navigation", () => ({
    useRouter: jest.fn(),
}));

jest.mock("next/client", () => ({
    router: {
        push: jest.fn(),
    },
}));

import { router as mockClientRouter } from "next/client";

describe("ForgotPasswordPage", () => {
    beforeEach(() => {
        jest.clearAllMocks();
        jest.useFakeTimers();
    });

    afterEach(() => {
        jest.useRealTimers();
    });

    it("renders the forgot password form correctly", () => {
        render(<ForgotPasswordPage />);
        expect(screen.getByText(/forgot password/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    });

    it("successfully sends reset link and redirects after timeout", async () => {
        (authApi.forgotPassword as jest.Mock).mockResolvedValue({});

        render(<ForgotPasswordPage />);

        const emailInput = screen.getByLabelText(/email/i);
        const submitBtn = screen.getByRole("button", { name: /send reset link/i });

        fireEvent.change(emailInput, { target: { value: "test@example.com" } });
        fireEvent.click(submitBtn);

        expect(submitBtn).toBeDisabled();
        expect(screen.getByText(/sending/i)).toBeInTheDocument();

        const successMsg = await screen.findByText(/if an account with this email exists/i);
        expect(successMsg).toBeInTheDocument();
        expect(authApi.forgotPassword).toHaveBeenCalledWith("test@example.com");

        act(() => {
            jest.advanceTimersByTime(1500);
        });

        expect(mockClientRouter.push).toHaveBeenCalledWith("/login");
    });

    it("displays error message when the API request fails", async () => {
        const errorMessage = "Service unavailable";
        (authApi.forgotPassword as jest.Mock).mockRejectedValue({ message: errorMessage });

        render(<ForgotPasswordPage />);

        const emailInput = screen.getByLabelText(/email/i);
        const submitBtn = screen.getByRole("button", { name: /send reset link/i });

        fireEvent.change(emailInput, { target: { value: "fail@example.com" } });
        fireEvent.click(submitBtn);

        const errorDisplay = await screen.findByText(errorMessage);
        expect(errorDisplay).toBeInTheDocument();
        expect(submitBtn).not.toBeDisabled();
    });
});
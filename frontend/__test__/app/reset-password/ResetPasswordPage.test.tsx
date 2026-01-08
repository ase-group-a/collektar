import React from "react";
import { render, screen, fireEvent, waitFor, act } from "@testing-library/react";
import ResetPasswordPage from "@/app/reset-password/page";
import { useSearchParams, useRouter } from "next/navigation";
import { authApi } from "@/lib/api/authApi";

jest.mock("next/navigation", () => ({
    useSearchParams: jest.fn(),
    useRouter: jest.fn(),
}));

jest.mock("@/lib/api/authApi", () => ({
    authApi: {
        resetPassword: jest.fn(),
    },
}));

describe("ResetPasswordPage", () => {
    const mockPush = jest.fn();
    const mockGet = jest.fn();

    beforeEach(() => {
        jest.clearAllMocks();
        jest.useFakeTimers();
        (useRouter as jest.Mock).mockReturnValue({ push: mockPush });
        (useSearchParams as jest.Mock).mockReturnValue({ get: mockGet });
    });

    afterEach(() => {
        jest.useRealTimers();
    });

    it("shows error if passwords do not match", async () => {
        mockGet.mockReturnValue("valid-token");
        render(<ResetPasswordPage />);

        const newPassInput = screen.getByLabelText("New Password");
        const repeatPassInput = screen.getByLabelText("Repeat Password");
        const submitBtn = screen.getByRole("button", { name: /reset password/i });

        fireEvent.change(newPassInput, { target: { value: "pass123" } });
        fireEvent.change(repeatPassInput, { target: { value: "pass456" } });
        fireEvent.click(submitBtn);

        expect(await screen.findByText("Passwords do not match.")).toBeInTheDocument();
    });

    it("displays API error message on failure", async () => {
        mockGet.mockReturnValue("valid-token");
        (authApi.resetPassword as jest.Mock).mockRejectedValue({ message: "Token expired" });

        render(<ResetPasswordPage />);

        fireEvent.change(screen.getByLabelText("New Password"), { target: { value: "password" } });
        fireEvent.change(screen.getByLabelText("Repeat Password"), { target: { value: "password" } });
        fireEvent.click(screen.getByRole("button", { name: /reset password/i }));

        expect(await screen.findByText("Token expired")).toBeInTheDocument();
    });
});
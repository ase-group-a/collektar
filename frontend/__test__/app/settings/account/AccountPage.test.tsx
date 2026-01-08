import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import AccountPage from "@/app/settings/account/page";
import { useAuth } from "@/lib/auth/AuthProvider";
import { authApi } from "@/lib/api/authApi";
import { useRouter } from "next/navigation";

jest.mock("@/lib/auth/AuthProvider", () => ({
    useAuth: jest.fn(),
}));

jest.mock("@/lib/api/authApi", () => ({
    authApi: {
        changePassword: jest.fn(),
        deleteAccount: jest.fn(),
    },
}));

jest.mock("next/navigation", () => ({
    useRouter: jest.fn(),
}));

describe("AccountPage", () => {
    const mockLogout = jest.fn();
    const mockReplace = jest.fn();
    const mockUser = {
        username: "testuser",
        display_name: "Test User",
        email: "test@example.com",
    };

    beforeEach(() => {
        jest.clearAllMocks();
        (useAuth as jest.Mock).mockReturnValue({
            user: mockUser,
            logout: mockLogout,
        });
        (useRouter as jest.Mock).mockReturnValue({
            replace: mockReplace,
        });
    });

    it("renders user information correctly", () => {
        render(<AccountPage />);

        expect(screen.getByDisplayValue("testuser")).toBeInTheDocument();
        expect(screen.getByDisplayValue("Test User")).toBeInTheDocument();
        expect(screen.getByDisplayValue("test@example.com")).toBeInTheDocument();
    });

    it("displays error when new passwords do not match", async () => {
        render(<AccountPage />);

        const newPassInput = screen.getByLabelText("New Password");
        const repeatPassInput = screen.getByLabelText("Repeat New Password");
        const changeBtn = screen.getByRole("button", { name: /change password/i });

        fireEvent.change(newPassInput, { target: { value: "password123" } });
        fireEvent.change(repeatPassInput, { target: { value: "password456" } });
        fireEvent.click(changeBtn);

        await waitFor(() => {
            expect(screen.getByText("Passwords do not match.")).toBeInTheDocument();
        });
    });

    it("calls changePassword api and clears inputs on success", async () => {
        (authApi.changePassword as jest.Mock).mockResolvedValue({});
        render(<AccountPage />);

        const currentPassInput = screen.getByLabelText("Current Password");
        const newPassInput = screen.getByLabelText("New Password");
        const repeatPassInput = screen.getByLabelText("Repeat New Password");
        const changeBtn = screen.getByRole("button", { name: /change password/i });

        fireEvent.change(currentPassInput, { target: { value: "oldPass" } });
        fireEvent.change(newPassInput, { target: { value: "newPass" } });
        fireEvent.change(repeatPassInput, { target: { value: "newPass" } });
        fireEvent.click(changeBtn);

        await waitFor(() => {
            expect(authApi.changePassword).toHaveBeenCalledWith("oldPass", "newPass");
            expect(screen.getByText("Password changed successfully.")).toBeInTheDocument();
            expect(currentPassInput).toHaveValue("");
            expect(newPassInput).toHaveValue("");
            expect(repeatPassInput).toHaveValue("");
        });
    });

    it("displays error message when changePassword api fails", async () => {
        const errorMessage = "Invalid current password";
        (authApi.changePassword as jest.Mock).mockRejectedValue({ message: errorMessage });

        render(<AccountPage />);

        const newPassInput = screen.getByLabelText("New Password");
        const repeatPassInput = screen.getByLabelText("Repeat New Password");
        const changeBtn = screen.getByRole("button", { name: /change password/i });

        fireEvent.change(newPassInput, { target: { value: "123" } });
        fireEvent.change(repeatPassInput, { target: { value: "123" } });
        fireEvent.click(changeBtn);

        await waitFor(() => {
            expect(screen.getByText(errorMessage)).toBeInTheDocument();
        });
    });

    it("displays error when deleting account without password", async () => {
        render(<AccountPage />);

        const deleteBtn = screen.getByRole("button", { name: /delete account/i });
        fireEvent.click(deleteBtn);

        await waitFor(() => {
            expect(screen.getByText("Please enter your password to delete your account.")).toBeInTheDocument();
        });
    });

    it("successfully deletes account and redirects", async () => {
        (authApi.deleteAccount as jest.Mock).mockResolvedValue({});
        render(<AccountPage />);

        const passwordInput = screen.getByPlaceholderText("Enter your password");
        const deleteBtn = screen.getByRole("button", { name: /delete account/i });

        fireEvent.change(passwordInput, { target: { value: "secret123" } });
        fireEvent.click(deleteBtn);

        await waitFor(() => {
            expect(authApi.deleteAccount).toHaveBeenCalledWith("secret123");
            expect(mockLogout).toHaveBeenCalled();
            expect(mockReplace).toHaveBeenCalledWith("/login");
        });
    });
});
import { render, screen, fireEvent } from "@testing-library/react";
import AccountPage from "@/app/settings/account/page";
import { useAuth } from "@/lib/auth/AuthProvider";

jest.mock("@/lib/auth/AuthProvider", () => ({
    useAuth: jest.fn(),
}));

describe("AccountPage", () => {
    const mockUser = {
        username: "user1",
        display_name: "John Doe",
        email: "user1@example.com",
    };

    beforeEach(() => {
        (useAuth as jest.Mock).mockReturnValue({ user: mockUser });
        jest.clearAllMocks();
    });

    it("renders user information in inputs", () => {
        render(<AccountPage />);

        expect(screen.getByPlaceholderText("Username")).toHaveValue("user1");
        expect(screen.getByPlaceholderText("Display Name")).toHaveValue("John Doe");
        expect(screen.getByPlaceholderText("email@example.com")).toHaveValue("user1@example.com");

        const buttons = screen.getAllByRole("button");
        expect(buttons.some(btn => btn.textContent === "Save")).toBe(true);
        expect(buttons.some(btn => btn.textContent === "Change Password")).toBe(true);
    });

    it("allows changing input values", () => {
        render(<AccountPage />);

        const usernameInput = screen.getByPlaceholderText("Username");
        const displayInput = screen.getByPlaceholderText("Display Name");
        const emailInput = screen.getByPlaceholderText("email@example.com");
        const newPasswordInput = screen.getByPlaceholderText("New password");
        const repeatPasswordInput = screen.getByPlaceholderText("Repeat new password");

        fireEvent.change(usernameInput, { target: { value: "user2" } });
        fireEvent.change(displayInput, { target: { value: "Jane Doe" } });
        fireEvent.change(emailInput, { target: { value: "user2@example.com" } });
        fireEvent.change(newPasswordInput, { target: { value: "newpass123" } });
        fireEvent.change(repeatPasswordInput, { target: { value: "newpass123" } });

        expect(usernameInput).toHaveValue("user2");
        expect(displayInput).toHaveValue("Jane Doe");
        expect(emailInput).toHaveValue("user2@example.com");
        expect(newPasswordInput).toHaveValue("newpass123");
        expect(repeatPasswordInput).toHaveValue("newpass123");
    });
});

import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import RegisterPage from "@/app/register/page";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";

jest.mock("next/navigation", () => ({
    useRouter: jest.fn(),
}));

const mockPush = jest.fn();
(useRouter as jest.Mock).mockReturnValue({ push: mockPush });

const mockRegister = jest.fn();
jest.mock("@/lib/auth/AuthProvider", () => ({
    useAuth: jest.fn(),
}));
(useAuth as jest.Mock).mockReturnValue({ register: mockRegister });

describe("RegisterPage", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    it("renders form fields and google logo", () => {
        render(<RegisterPage />);
        expect(screen.getByPlaceholderText("email@example.com")).toBeInTheDocument();
        expect(screen.getByPlaceholderText("Your unique username")).toBeInTheDocument();
        expect(screen.getByPlaceholderText("Your display name")).toBeInTheDocument();
        expect(screen.getByPlaceholderText("Create a password")).toBeInTheDocument();
        expect(screen.getByPlaceholderText("Repeat your password")).toBeInTheDocument();
        expect(screen.getByAltText(/Google logo/i)).toBeInTheDocument();
    });

    it("shows password mismatch error", async () => {
        render(<RegisterPage />);
        fireEvent.change(screen.getByPlaceholderText("email@example.com"), { target: { value: "test@example.com" } });
        fireEvent.change(screen.getByPlaceholderText("Your unique username"), { target: { value: "user123" } });
        fireEvent.change(screen.getByPlaceholderText("Your display name"), { target: { value: "Test User" } });
        fireEvent.change(screen.getByPlaceholderText("Create a password"), { target: { value: "password1" } });
        fireEvent.change(screen.getByPlaceholderText("Repeat your password"), { target: { value: "password2" } });
        fireEvent.click(screen.getByRole("button", { name: /Create account/i }));
        expect(await screen.findByText(/Passwords do not match/i)).toBeInTheDocument();

    });

    it("does not call register when validation fails", async () => {
        render(<RegisterPage />);
        fireEvent.click(screen.getByRole("button", { name: /Create account/i }));
        await waitFor(() => {
            expect(mockRegister).not.toHaveBeenCalled();
        });
    });

    it("calls register and redirects on success", async () => {
        mockRegister.mockResolvedValueOnce(undefined);
        render(<RegisterPage />);
        fireEvent.change(screen.getByPlaceholderText("email@example.com"), { target: { value: "test@example.com" } });
        fireEvent.change(screen.getByPlaceholderText("Your unique username"), { target: { value: "tester" } });
        fireEvent.change(screen.getByPlaceholderText("Your display name"), { target: { value: "Tester" } });
        fireEvent.change(screen.getByPlaceholderText("Create a password"), { target: { value: "longpassword" } });
        fireEvent.change(screen.getByPlaceholderText("Repeat your password"), { target: { value: "longpassword" } });
        fireEvent.click(screen.getByRole("button", { name: /Create account/i }));
        await waitFor(() => {
            expect(mockRegister).toHaveBeenCalledWith("test@example.com", "tester", "Tester", "longpassword");
            expect(mockPush).toHaveBeenCalledWith("/u/tester");
        });
    });

    it("displays server error when register rejects", async () => {
        mockRegister.mockRejectedValueOnce(new Error("Server failed"));
        render(<RegisterPage />);
        fireEvent.change(screen.getByPlaceholderText("email@example.com"), { target: { value: "a@b.com" } });
        fireEvent.change(screen.getByPlaceholderText("Your unique username"), { target: { value: "abc" } });
        fireEvent.change(screen.getByPlaceholderText("Your display name"), { target: { value: "ABC" } });
        fireEvent.change(screen.getByPlaceholderText("Create a password"), { target: { value: "longpass" } });
        fireEvent.change(screen.getByPlaceholderText("Repeat your password"), { target: { value: "longpass" } });
        fireEvent.click(screen.getByRole("button", { name: /Create account/i }));
        expect(await screen.findByText("Server failed")).toBeInTheDocument();
    });

    it("toggles password visibility for both password fields", async () => {
        render(<RegisterPage />);

        const createPwdInput = screen.getByPlaceholderText("Create a password") as HTMLInputElement;
        const repeatPwdInput = screen.getByPlaceholderText("Repeat your password") as HTMLInputElement;

        expect(createPwdInput.type).toBe("password");
        expect(repeatPwdInput.type).toBe("password");

        const createPwdToggle = createPwdInput.parentElement!.querySelector('button')!;
        fireEvent.click(createPwdToggle);

        await screen.findByLabelText(/Hide password/i);

        expect((screen.getByPlaceholderText("Create a password") as HTMLInputElement).type).toBe("text");

        const repeatPwdToggle = repeatPwdInput.parentElement!.querySelector('button')!;
        fireEvent.click(repeatPwdToggle);

        await screen.findAllByLabelText(/Hide password/i);

        expect((screen.getByPlaceholderText("Repeat your password") as HTMLInputElement).type).toBe("text");

        fireEvent.click(createPwdToggle);
        fireEvent.click(repeatPwdToggle);
        expect((screen.getByPlaceholderText("Create a password") as HTMLInputElement).type).toBe("password");
        expect((screen.getByPlaceholderText("Repeat your password") as HTMLInputElement).type).toBe("password");
    });


});

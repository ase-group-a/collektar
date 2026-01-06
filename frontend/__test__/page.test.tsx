import '@testing-library/jest-dom';
import { render, screen } from '@testing-library/react';
import Page from '../app/page';

describe('Homepage', () => {
    it('renders the logo', () => {
        render(<Page />);
        const logo = screen.getByAltText('Collektar logo');
        expect(logo).toBeInTheDocument();
    });

    it('renders category buttons', () => {
        render(<Page />);
        const categories = ['Games', 'Movies', 'Shows', 'Music', 'Books', 'Boardgames'];
        categories.forEach((text) => {
            const button = screen.getByRole('link', { name: text });
            expect(button).toBeInTheDocument();
        });
    });

    it('renders login and sign up buttons', () => {
        render(<Page />);
        const loginButton = screen.getByRole('link', { name: /log in/i });
        const signupButton = screen.getByRole('link', { name: /sign up/i });
        expect(loginButton).toBeInTheDocument();
        expect(signupButton).toBeInTheDocument();
    });

    it('renders features section', () => {
        render(<Page />);
        expect(screen.getByText(/search public apis/i)).toBeInTheDocument();
        expect(screen.getByText(/create custom collections/i)).toBeInTheDocument();
    });

    it('renders footer with repository link', () => {
        render(<Page />);
        const repoLink = screen.getByRole('link', { name: /repository/i });
        expect(repoLink).toBeInTheDocument();
    });
});

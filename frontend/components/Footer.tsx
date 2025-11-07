export default function Footer() {
    return (
        <footer className="footer footer-center bg-base-200  p-6 mt-8">
            <div>
                <p className="text-sm opacity-80">
                    © {new Date().getFullYear()} <span >Collektar</span>.
                </p>
            </div>

        </footer>
    );
}

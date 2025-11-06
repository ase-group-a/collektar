export default function Footer() {
    return (
        <footer className="bg-neutral text-neutral-content py-6 mt-12">
            <div className="container mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-4">
                <p className="text-sm opacity-80">
                    © {new Date().getFullYear()} Collektar.
                </p>

                <div className="flex gap-6 text-sm">
                    <a href="#" className="hover:text-primary transition-colors">
                        Privacy
                    </a>
                </div>
            </div>
        </footer>
    );
}

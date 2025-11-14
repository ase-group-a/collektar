interface PaginationProps {
    page: number;
    totalPages: number;
    onPageChange: (newPage: number) => void;
}

const Pagination = ({ page, totalPages, onPageChange }: PaginationProps) => {
    const pages: (number | string)[] = [];
    const maxButtons = 5;

    if (totalPages <= maxButtons) {
        for (let i = 1; i <= totalPages; i++) pages.push(i);
    } else {
        pages.push(1);
        if (page > 3) pages.push("...");
        for (let i = Math.max(2, page - 1); i <= Math.min(totalPages - 1, page + 1); i++) {
            pages.push(i);
        }
        if (page < totalPages - 2) pages.push("...");
        pages.push(totalPages);
    }

    return (


        <div className="join mt-4">
            <button
                className="join-item btn"
                disabled={page === 1}
                onClick={() => onPageChange(page - 1)}
            >
                «
            </button>

            {pages.map((p, idx) =>
                typeof p === "number" ? (
                    <button
                        key={idx}
                        className={`join-item btn ${p === page ? "btn-active" : ""}`}
                        onClick={() => onPageChange(p)}
                    >
                        {p}
                    </button>
                ) : (
                    <button key={idx} className="join-item btn btn-disabled">{p}</button>
                )
            )}

            <button
                className="join-item btn"
                disabled={page === totalPages}
                onClick={() => onPageChange(page + 1)}
            >
                »
            </button>
        </div>
    );
};

export default Pagination;

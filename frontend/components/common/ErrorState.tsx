import React from "react";
import StatusWrapper from "./StatusWrapper";

interface ErrorStateProps {
    message?: string;
    onRetry?: () => void;
}

const ErrorState: React.FC<ErrorStateProps> = ({
                                                   message = "An unexpected error occurred.",
                                                   onRetry,
                                               }) => (
    <StatusWrapper>
        <div className="space-y-4">
            <p className="text-lg font-semibold text-error">Failed to load media.</p>
            <p className="text-sm opacity-70">{message}</p>
            {onRetry && (
                <button className="btn btn-wide btn-sm" onClick={onRetry}>
                    Retry
                </button>
            )}
        </div>
    </StatusWrapper>
);

export default ErrorState;

import React from "react";

interface StatusWrapperProps {
    children: React.ReactNode;
}

const StatusWrapper: React.FC<StatusWrapperProps> = ({ children }) => (
    <div className="flex items-center justify-center h-[80vh] w-full">
        <div className="text-center text-base-content">{children}</div>
    </div>
);

export default StatusWrapper;

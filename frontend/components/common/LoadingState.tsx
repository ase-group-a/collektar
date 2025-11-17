import React from "react";
import StatusWrapper from "./StatusWrapper";

const LoadingState = () => (
    <StatusWrapper>
    <span
        className="loading loading-spinner loading-xl"
        role="status"
        aria-label="Loading"
    ></span>
    </StatusWrapper>
);

export default LoadingState;

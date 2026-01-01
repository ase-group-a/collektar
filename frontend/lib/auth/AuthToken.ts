let token: string | null = null;

export const setAccessToken = (t: string | null) => {
    token = t;
};

export const getAccessToken = (): string | null => token;

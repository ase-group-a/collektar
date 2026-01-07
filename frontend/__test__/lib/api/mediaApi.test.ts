import { searchMedia } from "@/lib/api/mediaApi";
import { fetcher } from "@/lib/fetcher";
import { MediaItem, SearchResult } from "@/types/media";

jest.mock("@/lib/fetcher");

const mockedFetcher = fetcher as jest.MockedFunction<typeof fetcher>;

describe("searchMedia", () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it("calls fetcher with the correct path and returns the result", async () => {
        const mockResult: SearchResult<MediaItem> = {
            total: 1,
            limit: 10,
            offset: 0,
            items: [
                {
                    id: "1",
                    title: "Test Movie",
                } as MediaItem,
            ],
        };

        mockedFetcher.mockResolvedValueOnce(mockResult);

        const result = await searchMedia("movies", "test", 10, 0);

        expect(mockedFetcher).toHaveBeenCalledTimes(1);
        expect(mockedFetcher).toHaveBeenCalledWith(
            "/api/media/movies?q=test&limit=10&offset=0"
        );
        expect(result).toEqual(mockResult);
    });

    it("URL-encodes the query string", async () => {
        mockedFetcher.mockResolvedValueOnce({} as SearchResult<MediaItem>);

        await searchMedia("games", "hello world");

        expect(mockedFetcher).toHaveBeenCalledWith(
            "/api/media/games?q=hello%20world&limit=10&offset=0"
        );
    });

    it("uses default limit and offset when not provided", async () => {
        mockedFetcher.mockResolvedValueOnce({} as SearchResult<MediaItem>);

        await searchMedia("books", "harry potter");

        expect(mockedFetcher).toHaveBeenCalledWith(
            "/api/media/books?q=harry%20potter&limit=10&offset=0"
        );
    });
});
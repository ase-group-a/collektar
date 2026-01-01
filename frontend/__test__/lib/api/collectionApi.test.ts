jest.mock("@/lib/fetcher", () => ({
    fetcher: jest.fn(),
}));

import { fetcher } from "@/lib/fetcher";
import * as collectionApi from "@/lib/api/collectionApi";

const mockedFetcher = fetcher as unknown as jest.Mock;

describe("collectionApi", () => {
    beforeEach(() => {
        mockedFetcher.mockReset();
    });

    it("getCollections calls fetcher with correct path", async () => {
        const fake = [{ id: "c1", type: "movie" }];
        mockedFetcher.mockResolvedValueOnce(fake);

        const res = await collectionApi.getCollections();

        expect(mockedFetcher).toHaveBeenCalledWith("/api/collection/collections");
        expect(res).toEqual(fake);
    });

    it("addItemToCollection posts to lowercased type path", async () => {
        const returnItem = { item_id: "i1", id: "ri1" };
        mockedFetcher.mockResolvedValueOnce(returnItem);

        const result = await collectionApi.addItemToCollection(
            "MOVIE",
            "i1",
            "Title",
            "/img.jpg",
            "desc",
            "src"
        );

        const [path, init] = mockedFetcher.mock.calls[0];
        expect(path).toBe("/api/collection/collections/movie/items");
        expect(init.method).toBe("POST");
        expect(JSON.parse(init.body as string)).toEqual({
            item_id: "i1",
            title: "Title",
            image_url: "/img.jpg",
            description: "desc",
            source: "src",
        });

        expect(result).toEqual(returnItem);
    });

    it("removeItemFromCollection sends DELETE", async () => {
        mockedFetcher.mockResolvedValueOnce(undefined);

        await collectionApi.removeItemFromCollection("MOVIE", "i1");

        expect(mockedFetcher).toHaveBeenCalledWith(
            "/api/collection/collections/movie/items/i1",
            { method: "DELETE" }
        );
    });
});

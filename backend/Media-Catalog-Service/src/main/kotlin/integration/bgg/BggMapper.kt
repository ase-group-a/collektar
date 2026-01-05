// src/main/kotlin/integration/bgg/BggMapper.kt
package integration.bgg

import domain.MediaItem
import domain.MediaType
import domain.SearchResult
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

object BggMapper {

    data class SearchHit(
        val id: Long,
        val title: String,
        val year: Int?
    )

    data class ThingInfo(
        val id: Long,
        val imageUrl: String?,
        val thumbnailUrl: String?,
        val description: String?
    )

    /**
     * Parse /xmlapi2/search response.
     * NOTE: no images here.
     */
    fun parseSearchHits(xml: String): List<SearchHit> {
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        return doc.select("items > item").mapNotNull { item ->
            val id = item.attr("id").toLongOrNull() ?: return@mapNotNull null

            val name = item.selectFirst("name[type=primary]")?.attr("value")
                ?: item.selectFirst("name")?.attr("value")
                ?: "Unknown"

            val year = item.selectFirst("yearpublished")?.attr("value")?.toIntOrNull()

            SearchHit(
                id = id,
                title = name,
                year = year
            )
        }
    }

    /**
     * Parse /xmlapi2/thing response (supports multiple ids).
     * Extracts image/thumbnail/description per item.
     */
    fun parseThings(xml: String): Map<Long, ThingInfo> {
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        return doc.select("items > item").mapNotNull { item ->
            val id = item.attr("id").toLongOrNull() ?: return@mapNotNull null

            val image = item.selectFirst("image")?.text()?.trim().takeIf { !it.isNullOrBlank() }
            val thumb = item.selectFirst("thumbnail")?.text()?.trim().takeIf { !it.isNullOrBlank() }
            val desc = item.selectFirst("description")?.text()?.trim().takeIf { !it.isNullOrBlank() }

            ThingInfo(
                id = id,
                imageUrl = image,
                thumbnailUrl = thumb,
                description = desc
            )
        }.associateBy { it.id }
    }

    /**
     * Build SearchResult (paged) with images merged from ThingInfo map.
     */
    fun toSearchResultWithImages(
        allHits: List<SearchHit>,
        pageHits: List<SearchHit>,
        thingInfo: Map<Long, ThingInfo>,
        limit: Int,
        offset: Int
    ): SearchResult {

        val items = pageHits.map { hit ->
            val info = thingInfo[hit.id]
            MediaItem(
                id = "bgg:${hit.id}",
                title = hit.title,
                type = MediaType.BOARDGAME,
                imageUrl = info?.thumbnailUrl ?: info?.imageUrl,
                description = info?.description,
                source = "bgg"
            )
        }

        return SearchResult(
            total = allHits.size,
            limit = limit,
            offset = offset,
            items = items
        )
    }
}

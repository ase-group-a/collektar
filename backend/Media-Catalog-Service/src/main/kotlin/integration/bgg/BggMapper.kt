package integration.bgg

import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageSource
import domain.MediaItem
import domain.MediaType
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import java.io.StringReader
import org.xml.sax.InputSource

object BggMapper {

    fun parseSearchIds(xml: String): Pair<Int, List<Long>> {
        val doc = parseXml(xml)
        val items = doc.getElementsByTagName("item")
        val total = items.length

        val ids = (0 until items.length).mapNotNull { idx ->
            val el = items.item(idx) as? Element ?: return@mapNotNull null
            el.getAttribute("id")?.toLongOrNull()
        }

        return total to ids
    }

    fun mapHotResponse(xml: String, imageCacheClient: ImageCacheClient? = null): List<MediaItem> {
        val doc = parseXml(xml)
        val items = doc.getElementsByTagName("item")

        return (0 until items.length).mapNotNull { idx ->
            val el = items.item(idx) as? Element ?: return@mapNotNull null
            val id = el.getAttribute("id")?.toLongOrNull() ?: return@mapNotNull null

            val name = el.getElementsByTagName("name")
                .item(0)
                ?.let { it as? Element }
                ?.getAttribute("value")
                ?.takeIf { it.isNotBlank() }
                ?: "Unknown"

            val thumb = el.getElementsByTagName("thumbnail")
                .item(0)
                ?.let { it as? Element }
                ?.getAttribute("value")
                ?.trim()
                ?.takeIf { it.isNotBlank() }

            val imageUrl = thumb?.let {
                imageCacheClient?.getImageUrl(ImageSource.BGG, it) ?: it
            }

            MediaItem(
                id = "bgg:$id",
                title = name,
                type = MediaType.BOARDGAME,
                imageUrl = imageUrl,
                description = null,
                source = "BGG"
            )
        }
    }

    fun mapThingListResponse(xml: String, imageCacheClient: ImageCacheClient? = null): List<MediaItem> {
        val doc = parseXml(xml)
        val items = doc.getElementsByTagName("item")

        return (0 until items.length).mapNotNull { idx ->
            val el = items.item(idx) as? Element ?: return@mapNotNull null
            val id = el.getAttribute("id")?.toLongOrNull() ?: return@mapNotNull null

            val primaryName = el.getElementsByTagName("name")
                .let { list ->
                    (0 until list.length)
                        .mapNotNull { i -> list.item(i) as? Element }
                        .firstOrNull { it.getAttribute("type") == "primary" }
                }
                ?.getAttribute("value")
                ?.takeIf { it.isNotBlank() }
                ?: "Unknown"

            val imageUrl = el.getElementsByTagName("image")
                .item(0)
                ?.textContent
                ?.trim()
                ?.takeIf { it.isNotBlank() }

            val thumbUrl = el.getElementsByTagName("thumbnail")
                .item(0)
                ?.textContent
                ?.trim()
                ?.takeIf { it.isNotBlank() }

            val originalUrl = imageUrl ?: thumbUrl
            val cachedImageUrl = originalUrl?.let {
                imageCacheClient?.getImageUrl(ImageSource.BGG, it) ?: it
            }

            MediaItem(
                id = "bgg:$id",
                title = primaryName,
                type = MediaType.BOARDGAME,
                imageUrl = cachedImageUrl,
                description = null,
                source = "BGG"
            )
        }
    }

    private fun parseXml(xml: String) = try {
        val cleanXml = xml.trim().trimStart('\uFEFF')

        DocumentBuilderFactory.newInstance()
            .apply {
                isNamespaceAware = false
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            }
            .newDocumentBuilder()
            .parse(InputSource(StringReader(cleanXml)))
    } catch (e: Exception) {
        throw IllegalStateException("Failed to parse BGG XML response. First 500 chars: ${xml.take(500)}", e)
    }
}
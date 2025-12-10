package integration.bgg

import domain.MediaItem
import domain.MediaType
import domain.SearchResult
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.SAXParseException

object XmlUtil {

    fun parseOrNull(xml: String): Element? {
        if (xml.isBlank()) return null

        return try {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = false
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))
            doc.documentElement
        } catch (e: SAXParseException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}

object BggMapper {

    fun mapSearchResponse(
        xml: String,
        limit: Int,
        offset: Int
    ): SearchResult {
        val root = XmlUtil.parseOrNull(xml)
            ?: return SearchResult(
                total = 0,
                limit = limit,
                offset = offset,
                items = emptyList()
            )

        if (root.nodeName.equals("message", ignoreCase = true)) {
            return SearchResult(
                total = 0,
                limit = limit,
                offset = offset,
                items = emptyList()
            )
        }

        val itemsNodeList = root.getElementsByTagName("item")
        val allItems = mutableListOf<MediaItem>()

        for (i in 0 until itemsNodeList.length) {
            val item = itemsNodeList.item(i) as Element
            val id = item.getAttribute("id")

            val nameNodes = item.getElementsByTagName("name")
            val name = if (nameNodes.length > 0) {
                (nameNodes.item(0) as Element).getAttribute("value")
            } else {
                "Unknown"
            }

            allItems.add(
                MediaItem(
                    id = "bgg:$id",
                    title = name,
                    type = MediaType.BOARDGAME,
                    imageUrl = null,
                    description = null,
                    source = "BGG"
                )
            )
        }

        val totalAttr = root.getAttribute("total")
        val totalFromApi = totalAttr.toIntOrNull() ?: allItems.size

        val safeOffset = offset.coerceAtLeast(0)
        val fromIndex = safeOffset.coerceAtMost(allItems.size)
        val toIndex = (fromIndex + limit).coerceAtMost(allItems.size)

        val pageItems = if (fromIndex < toIndex) {
            allItems.subList(fromIndex, toIndex)
        } else {
            emptyList()
        }

        return SearchResult(
            total = totalFromApi,
            limit = limit,
            offset = safeOffset,
            items = pageItems
        )
    }

    fun mapThingResponse(xml: String): MediaItem? {
        val root = XmlUtil.parseOrNull(xml) ?: return null

        if (root.nodeName.equals("message", ignoreCase = true)) {
            return null
        }

        val itemsNodeList = root.getElementsByTagName("item")
        if (itemsNodeList.length == 0) return null

        val item = itemsNodeList.item(0) as Element
        val id = item.getAttribute("id")

        val nameNodes = item.getElementsByTagName("name")
        val primaryName = (0 until nameNodes.length)
            .asSequence()
            .map { nameNodes.item(it) as Element }
            .firstOrNull { it.getAttribute("type") == "primary" }
            ?: (nameNodes.item(0) as? Element)

        val name = primaryName?.getAttribute("value") ?: "Unknown"

        val imageNodes = item.getElementsByTagName("image")
        val imageUrl = if (imageNodes.length > 0) {
            imageNodes.item(0).textContent
        } else null

        val descNodes = item.getElementsByTagName("description")
        val rawDescription = if (descNodes.length > 0) {
            descNodes.item(0).textContent
        } else null

        return MediaItem(
            id = "bgg:$id",
            title = name,
            type = MediaType.BOARDGAME,
            imageUrl = imageUrl,
            description = rawDescription,
            source = "BGG"
        )
    }
}

package world.gregs.voidps.tools.wiki.model

import org.w3c.dom.Node
import world.gregs.voidps.tools.wiki.model.Wiki.Companion.getChildren
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class WikiPageRevision(
    val id: Long,
    val parentId: Int,
    val timestamp: LocalDate,
    val text: String,
) {

    companion object {
        private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)!!

        operator fun invoke(node: Node): WikiPageRevision {
            val children = node.getChildren()
            return WikiPageRevision(
                id = children.first { it.nodeName == "id" }.textContent.toLong(),
                parentId = children.firstOrNull { it.nodeName == "parentid" }?.textContent?.toInt() ?: -1,
                timestamp = LocalDate.parse(children.first { it.nodeName == "timestamp" }.textContent, inputFormatter),
                text = children.first { it.nodeName == "text" }.textContent,
            )
        }
    }
}

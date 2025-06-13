package world.gregs.voidps.tools.wiki.model

import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class Wiki(private val doc: Document) {

    val namespaces: List<WikiNamespace> by lazy {
        val elements = doc.getElementsByTagName("namespace")
        (0 until elements.length).map { WikiNamespace(elements.item(it)) }
    }

    fun namespace(name: String) = namespaces.first { it.name == name }

    val pages: List<WikiPage> by lazy {
        val elements = doc.getElementsByTagName("page")
        (0 until elements.length).map { WikiPage(elements.item(it), namespaces) }
    }

    fun page(name: String): WikiPage {
        val pages = pages
        val page = pages.firstOrNull { it.title == name } ?: pages.first { it.title.equals(name, true) }
        if (page.redirect.isNotBlank()) {
            return page(page.redirect)
        }
        return page
    }

    fun getPageOrNull(name: String): WikiPage? {
        val pages = pages
        val page = pages.firstOrNull { it.title == name } ?: pages.firstOrNull { it.title.equals(name, true) } ?: return null
        if (page.redirect.isNotBlank() && page.title.equals(page.redirect, true)) {
            return try {
                getPageOrNull(page.redirect)
            } catch (e: StackOverflowError) {
                println("Overflow ${page.title} ${page.redirect}")
                null
            }
        }
        return page
    }

    val redirectPattern = "#(?:REDIRECT|redirect) ?\\[\\[(.*)]]".toRegex()

    fun getExactPageOrNull(name: String): WikiPage? {
        val pages = pages
        val page = pages.firstOrNull { it.title == name } ?: return null
        if (page.redirect.isNotBlank() && page.revision.text.contains(redirectPattern)) {
            val redirect = redirectPattern.find(page.revision.text)!!.groupValues[1]
            return try {
                getExactPageOrNull(redirect)
            } catch (e: StackOverflowError) {
                println("Overflow ${page.title} ${page.redirect}")
                null
            }
        }
        return page
    }

    fun exchange(item: String) = page("Exchange:${page(item).title}")

    fun transcript(npc: String) = page("Transcript:${page(npc).title}")

    fun charms(npc: String) = page("Charm:${page(npc).title}")

    fun template(name: String) = page("Template:${page(name).title}")

    companion object {

        fun Node.getChildren(): List<Node> = (0 until childNodes.length).map { index -> childNodes.item(index) }

        fun load(xmlFileDirectory: String): Wiki {
            val inputFile = File(xmlFileDirectory)
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile)
            return Wiki(doc)
        }

        fun loadText(text: String): Wiki {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(text)
            return Wiki(doc)
        }
    }
}

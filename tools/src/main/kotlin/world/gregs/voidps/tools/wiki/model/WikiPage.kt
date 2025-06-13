package world.gregs.voidps.tools.wiki.model

import org.sweble.wikitext.engine.PageId
import org.sweble.wikitext.engine.PageTitle
import org.sweble.wikitext.engine.WtEngine
import org.sweble.wikitext.engine.WtEngineImpl
import org.sweble.wikitext.engine.config.WikiConfig
import org.sweble.wikitext.engine.nodes.EngPage
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp
import org.sweble.wikitext.parser.nodes.*
import org.w3c.dom.Node
import world.gregs.voidps.tools.wiki.model.Wiki.Companion.getChildren

@Suppress("UNCHECKED_CAST")
data class WikiPage(
    val title: String,
    val namespace: WikiNamespace,
    val id: Int,
    val redirect: String,
    val revision: WikiPageRevision,
) {

    private val config: WikiConfig by lazy { DefaultConfigEnWp.generate() }

    private val engine: WtEngine by lazy { WtEngineImpl(config) }

    private val pageTitle: PageTitle by lazy { PageTitle.make(config, title) }

    private val pageId: PageId by lazy { PageId(pageTitle, revision.id) }

    private val page: EngPage by lazy { engine.parse(pageId, revision.text, null).page }

    val templates: List<Pair<String, Any>> by lazy {
        content.filterIsInstance<WtTemplate>().mapNotNull { template ->
            if (template.name.isResolved) {
                val name = template.name.asString.trim()
                val arguments = template.args

                name to getTemplate(arguments)
            } else {
                null
            }
        }
    }

    private fun getTemplate(arguments: WtTemplateArguments): Any = if (arguments.any { it is WtTemplateArgument && it.hasName() }) {
        arguments.filterIsInstance<WtTemplateArgument>().associate { arg -> unwrap(arg[0] as WtName) to unwrapValue(arg[1] as WtValue) }
    } else {
        arguments.filterIsInstance<WtTemplateArgument>().map { arg -> unwrap(arg[1] as WtValue) }
    }

    val redirected: Boolean = revision.text.contains(redirectPattern)

    fun getRedirect(wiki: Wiki): WikiPage? {
        val redirect = redirectPattern.find(revision.text)!!.groupValues[1]
        return try {
            wiki.getExactPageOrNull(redirect)
        } catch (e: StackOverflowError) {
            println("Overflow $title $redirect ${revision.text}")
            null
        }
    }

    fun getTemplateMap(name: String): Map<String, Any>? = templates.firstOrNull { it.first.contains(name, true) }?.second as? Map<String, Any>

    fun getTemplateList(name: String): List<Pair<String, Any>>? = templates.firstOrNull { it.first.contains(name, true) }?.second as? List<Pair<String, Any>>

    fun getTemplateMaps(name: String): List<Map<String, Any>> = templates.filter { it.first.contains(name, true) }.mapNotNull { it.second as? Map<String, Any> }

    private fun unwrap(node: WtNode): String {
        val first = node.firstOrNull() ?: return ""
        return when (first) {
            is WtText -> first.content.trim()
            is WtTagExtension -> first.body.content.trim()
            else -> ""
        }
    }

    private fun unwrapValue(node: WtNode): Any {
        val first = node.firstOrNull() ?: return ""
        return when {
            node.size > 1 -> {
                node.mapNotNull {
                    if (it is WtTemplate) {
                        if (it.name.isResolved) {
                            val arguments = it.args
                            getTemplate(arguments)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
            }
            first is WtText -> first.content.trim()
            first is WtTagExtension -> first.body.content.trim()
            else -> ""
        }
    }

    val tables: List<WikiPageTable> by lazy {
        content.filterIsInstance<WtTable>().map { WikiPageTable(it) }
    }

    /**
     * Get table by name of first header
     */
    fun table(name: String): WikiPageTable = tables.first { it.headers.first().equals(name, true) }

    val content: List<WtNode> by lazy {
        val list = mutableListOf<WtNode>()
        addRecursive(list, page)
        list
    }

    companion object {

        val redirectPattern = "#(?:REDIRECT|redirect) ?\\[\\[(.*)]]".toRegex()

        private fun addRecursive(list: MutableList<WtNode>, node: WtNode) {
            list.add(node)
            node.forEach {
                addRecursive(list, it)
            }
        }

        operator fun invoke(node: Node, namespaces: List<WikiNamespace>): WikiPage {
            val children = node.getChildren()
            return WikiPage(
                title = children.first { it.nodeName == "title" }.textContent,
                namespace = children.first { it.nodeName == "ns" }.textContent.toInt().let { id -> namespaces.firstOrNull { it.key == id } ?: namespaces.first { it.key == 0 } },
                id = children.first { it.nodeName == "id" }.textContent.toInt(),
                redirect = children.firstOrNull { it.nodeName == "redirect" }?.attributes?.getNamedItem("title")?.textContent ?: "",
                revision = WikiPageRevision(children.first { it.nodeName == "revision" }),
            )
        }
    }
}

package rs.dusk.tools.wiki.model

import org.sweble.wikitext.engine.PageId
import org.sweble.wikitext.engine.PageTitle
import org.sweble.wikitext.engine.WtEngine
import org.sweble.wikitext.engine.WtEngineImpl
import org.sweble.wikitext.engine.config.WikiConfig
import org.sweble.wikitext.engine.nodes.EngPage
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp
import org.sweble.wikitext.parser.nodes.*
import org.w3c.dom.Node
import rs.dusk.tools.wiki.model.Wiki.Companion.getChildren

data class WikiPage(
    val title: String,
    val namespace: WikiNamespace,
    val id: Int,
    val redirect: String,
    val revision: WikiPageRevision
) {

    private val config: WikiConfig by lazy { DefaultConfigEnWp.generate() }

    private val engine: WtEngine by lazy { WtEngineImpl(config) }

    private val pageTitle: PageTitle by lazy { PageTitle.make(config, title) }

    private val pageId: PageId by lazy { PageId(pageTitle, revision.id) }

    private val page: EngPage by lazy { engine.parse(pageId, revision.text, null).page }

    val templates: List<Pair<String, Any>> by lazy {
        content.filterIsInstance<WtTemplate>().map { template ->
            val name = template.name.asString.trim()
            val arguments = template.args

            name to if (arguments.isNotEmpty() && (arguments.first() as? WtTemplateArgument)?.hasName() != false) {
                arguments.filterIsInstance<WtTemplateArgument>().map { arg -> unwrap(arg[0] as WtName) to unwrap(arg[1] as WtValue) }.toMap()
            } else {
                arguments.filterIsInstance<WtTemplateArgument>().map { arg -> unwrap(arg[1] as WtValue) }
            }
        }
    }

    private fun unwrap(node: WtNode): String {
        return (node[0] as WtText).content.trim()
    }

    val tables: List<WikiPageTable> by lazy {
        content.filterIsInstance<WtTable>().map { WikiPageTable(it) }
    }

    /**
     * Get table by name of first header
     */
    fun table(name: String): WikiPageTable {
        return tables.first { it.headers.first().equals(name, true) }
    }

    val content: List<WtNode> by lazy {
        val list = mutableListOf<WtNode>()
        addRecursive(list, page)
        list
    }

    companion object {

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
                revision = WikiPageRevision(children.first { it.nodeName == "revision" })
            )
        }
    }
}
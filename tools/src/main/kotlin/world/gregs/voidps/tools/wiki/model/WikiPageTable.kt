package world.gregs.voidps.tools.wiki.model

import org.sweble.wikitext.parser.nodes.*

data class WikiPageTable(val headers: List<String>, val rows: List<List<String>>) {

    fun print() {
        headers.forEach {
            print("${it}\t\t")
        }
        println()
        rows.forEach { row ->
            val sb = StringBuilder()
            row.forEach { body ->
                sb.append(body)
                sb.append("\t\t")
            }
            println(sb.toString())
        }
    }

    fun column(name: String): List<String> {
        val index = headers.indexOfFirst { it.equals(name, true) }
        val column = mutableListOf<String>()
        rows.forEach {
            column.add(it[index])
        }
        return column
    }

    companion object {
        operator fun invoke(table: WtTable): WikiPageTable {
            val headers = mutableListOf<String>()
            val rows = mutableListOf<List<String>>()
            table.body.forEach { node ->
                processTable(node, headers, rows)
            }
            return WikiPageTable(headers, rows)
        }

        private fun processTable(node: WtNode, headers: MutableList<String>, rows: MutableList<List<String>>) {
            when (node) {
                is WtTableHeader -> {
                    val header = (node[1] as? WtBody)?.firstOrNull { it is WtText } as? WtText
                    if (header != null) {
                        headers.add(header.content)
                    }
                }
                is WtTableRow -> {
                    val body = node[1] as WtBody
                    val row = mutableListOf<String>()
                    body.forEach {
                        val cell = it as? WtTableCell ?: return@forEach
                        val body = cell.body
                        val sb = StringBuilder()
                        var start = false
                        body.forEach { node ->
                            when {
                                node is WtImStartTag -> start = true
                                node is WtImEndTag -> start = false
                                start -> {
                                    when (node) {
                                        is WtText -> {
                                            sb.append(node.content)
                                        }
                                        is WtInternalLink -> {
                                            if (node.hasTitle()) {
                                                sb.append((node.title[0] as WtText).content)
                                            } else {
                                                sb.append(node.target.asString)
                                            }
                                        }
                                        is WtImageLink -> {
                                            sb.append("[[${((node[0] as WtPageName)[0] as WtText).content}]]")
                                        }
                                        is WtTemplate -> {
                                            if (node.name.isResolved) {
                                                sb.append("{{${node.name.asString}}}")
                                            } else {
                                                sb.append("{{template}}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        row.add(sb.toString())
                    }
                    rows.add(row)
                }
            }
        }
    }
}

package world.gregs.voidps.web.site

/**
 * Parses a GitHub-wiki-style `_Sidebar.md` (headings + nested `-` lists of `[text](href)`
 * links) into a tree [Docs] renders as the docs nav, replacing the flat alphabetical list when
 * the wiki source provides one. Two-space indents mark nesting, matching how GitHub itself
 * renders wiki sidebars.
 */
sealed class SidebarEntry {
    data class Item(val text: String, val href: String) : SidebarEntry()
    data class Group(val title: String, val href: String?, val children: List<SidebarEntry>) : SidebarEntry()
}

private val headingLine = Regex("^(#{1,6})\\s+(.*)$")
private val linkText = Regex("""\[(.+?)]\((.+?)\)""")

private fun parseLink(text: String): Pair<String, String>? {
    val match = linkText.find(text) ?: return null
    val label = match.groupValues[1].replace("**", "").replace("*", "").trim()
    return label to match.groupValues[2].trim()
}

private fun indentOf(line: String): Int = line.takeWhile { it == ' ' }.length

private fun parseItems(lines: List<String>, start: Int, indent: Int): Pair<List<SidebarEntry>, Int> {
    val entries = mutableListOf<SidebarEntry>()
    var i = start
    while (i < lines.size) {
        val line = lines[i]
        if (line.isBlank()) {
            i++
            continue
        }
        if (indentOf(line) != indent || !line.trim().startsWith("- ")) {
            break
        }
        val content = line.trim().removePrefix("- ").trim()
        val link = parseLink(content)
        val (children, next) = parseItems(lines, i + 1, indent + 2)
        entries += if (children.isEmpty()) {
            SidebarEntry.Item(link?.first ?: content, link?.second ?: "#")
        } else {
            SidebarEntry.Group(link?.first ?: content, link?.second, children)
        }
        i = next
    }
    return entries to i
}

fun parseSidebar(markdown: String): List<SidebarEntry> {
    val lines = markdown.replace("\r\n", "\n").split("\n")
    val top = mutableListOf<SidebarEntry>()
    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        if (line.isBlank()) {
            i++
            continue
        }
        val heading = headingLine.find(line)
        if (heading != null) {
            val content = heading.groupValues[2].trim()
            val link = parseLink(content)
            val title = link?.first ?: content.replace("**", "").replace("*", "").trim()
            val (children, next) = parseItems(lines, i + 1, 0)
            top += SidebarEntry.Group(title, link?.second, children)
            i = next
        } else {
            val (children, next) = parseItems(lines, i, 0)
            if (next == i) {
                i++
            } else {
                top += children
                i = next
            }
        }
    }
    return top
}

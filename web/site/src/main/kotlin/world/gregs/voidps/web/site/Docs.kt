package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.web.site.components.*
import java.io.File

/**
 * Generates the docs section from the Markdown files in `.claude/` (the same reference
 * material [CLAUDE.md]'s "Further Reading" list points at), so the two never drift apart.
 * Each source becomes its own static page — sidebar nav, breadcrumb, "on this page" outline —
 * built entirely from [Ui] components and [SiteChrome], with the Markdown body dropped in as
 * a `.markdown-body`-scoped fragment styled by the same design tokens as the rest of the site.
 */
object Docs {

    private data class DocSource(val id: String, val title: String, val description: String, val file: File)

    fun generate(wikiDir: File, buildDir: File) {
        val docsDir = File(buildDir, "docs")
        docsDir.mkdirs()

        val sidebar = File(wikiDir, "_Sidebar.md").takeIf { it.exists() }?.let { parseSidebar(it.readText()) }

        // "_Sidebar.md" and "_Footer.md" are wiki chrome, not content pages.
        val sources = wikiDir.listFiles()!!
            .filter { it.isFile && it.extension == "md" && !it.name.startsWith("_") }
            .map {
                val (frontMatter, _) = extractFrontMatter(it.readText())
                DocSource(
                    id = it.nameWithoutExtension.lowercase(),
                    title = frontMatter.title ?: it.nameWithoutExtension.replace("-", " "),
                    description = frontMatter.description ?: "",
                    file = it,
                )
            }
            .sortedBy { it.title }

        val available = sources.filter { it.file.exists() }
        val ids = available.mapTo(mutableSetOf()) { it.id }
        val byId = available.associateBy { it.id }

        // Previous/next follows the curated reading order in `_Sidebar.md` (depth-first) rather
        // than the filesystem's incidental listing order. Docs that exist but aren't listed in
        // `_Sidebar.md` still get generated — they're appended afterwards, alphabetically by title.
        val sidebarOrder = sidebar?.let { flattenSidebar(it, ids) }?.distinct()?.mapNotNull { byId[it] } ?: emptyList()
        val order = sidebarOrder + available.filter { it.id !in sidebarOrder.mapTo(mutableSetOf()) { doc -> doc.id } }

        val searchIndex = mutableListOf<String>()
        for ((position, source) in order.withIndex()) {
            val (_, body) = extractFrontMatter(source.file.readText())
            val markdown = renderMarkdown(body)
            val previous = order.getOrNull(position - 1)
            val next = order.getOrNull(position + 1)
            val html = docPage(source, available, sidebar, markdown, previous, next)
            File(docsDir, "${source.id}.html").writeText(rewriteDocLinks(html, ids))
            searchIndex += indexEntryJson(source, markdown)
        }
        File(docsDir, "search-index.json").writeText(searchIndex.joinToString(",", "[", "]"))
        order.firstOrNull()?.let {
            File(docsDir, "index.html").writeText(File(docsDir, "${it.id}.html").readText())
        }
    }

    /** Resolves a sidebar/content link's `href` relative to `docs/`; external links pass through. */
    private fun resolveHref(href: String): String =
        if (href.startsWith("http://") || href.startsWith("https://")) href else href.removePrefix("./")

    /** Flattens `_Sidebar.md`'s tree into the depth-first order its entries appear in, keeping
     *  only hrefs that resolve to a known doc (drops external links and group labels with none). */
    private fun flattenSidebar(entries: List<SidebarEntry>, ids: Set<String>): List<String> {
        val result = mutableListOf<String>()
        fun visit(entry: SidebarEntry) {
            when (entry) {
                is SidebarEntry.Item -> {
                    val id = resolveHref(entry.href).lowercase()
                    if (id in ids) {
                        result += id
                    }
                }
                is SidebarEntry.Group -> {
                    if (entry.href != null) {
                        val id = resolveHref(entry.href).lowercase()
                        if (id in ids) {
                            result += id
                        }
                    }
                    entry.children.forEach(::visit)
                }
            }
        }
        entries.forEach(::visit)
        return result
    }

    /** Whether [entry] — or one of its descendants — links to [currentId], used to pre-expand the
     *  sidebar section the active page lives in. */
    private fun containsActive(entry: SidebarEntry, currentId: String): Boolean = when (entry) {
        is SidebarEntry.Item -> resolveHref(entry.href) == currentId
        is SidebarEntry.Group ->
            (entry.href != null && resolveHref(entry.href) == currentId) ||
                entry.children.any { containsActive(it, currentId) }
    }

    private val hrefAttribute = Regex("""href="([^"]*)"""")

    /**
     * Rewrites every internal `href` that names a known doc — written the GitHub-wiki way, with
     * no `.html` (`./roadmap`, `installation-guide#step-6...`) — to point straight at the real
     * `<id>.html` file. Doc pages used to instead ship an extensionless `docs/<id>` that redirected
     * client-side, but that meant every one of those clicks did two full page loads (and their own
     * un-styled first paint) instead of one — the visible flash was that second load, not the CSS.
     * Direct links avoid it entirely, and are just as robust for a plain static host as a redirect
     * would have been.
     */
    private fun rewriteDocLinks(html: String, ids: Set<String>): String = hrefAttribute.replace(html) { match ->
        val href = match.groupValues[1]
        if (href.isEmpty() || href.startsWith("#") || href.contains("://") || href.startsWith("mailto:")) {
            return@replace match.value
        }
        val hash = href.indexOf('#')
        val path = (if (hash >= 0) href.substring(0, hash) else href).removePrefix("./")
        val id = path.lowercase()
        if (id !in ids) {
            return@replace match.value
        }
        val fragment = if (hash >= 0) href.substring(hash) else ""
        """href="$id.html$fragment""""
    }

    /** `docs-nav-row` is the one look shared by every clickable line in the sidebar — a leaf link,
     *  a linked group's title, or a plain group's whole header button — so a collapsed section
     *  reads as just another row rather than a visually mismatched label. [depth] only ever adds
     *  the `docs-nav-depth-0` emphasis class; indentation itself comes purely from how deep the
     *  `<a>`/`<button>` sits inside nested `.docs-nav-children` columns (see docs.css), not from
     *  whether the entry started life as a `_Sidebar.md` heading or a nested list item. */
    private fun rowClasses(depth: Int, active: Boolean = false, extra: String = ""): String = buildString {
        append("docs-nav-row")
        if (depth == 0) {
            append(" docs-nav-depth-0")
        }
        if (active) {
            append(" active")
        }
        if (extra.isNotEmpty()) {
            append(' ')
            append(extra)
        }
    }

    private fun FlowContent.sidebarLink(text: String, href: String, active: Boolean, depth: Int) {
        a(href = href, classes = rowClasses(depth, active)) { +text }
    }

    /** Renders one `_Sidebar.md` entry. A [SidebarEntry.Group] becomes a collapsible section —
     *  collapsed by default, expanded if it contains the active page, toggled by [voidToggleDocsSection]
     *  in `docs.js`, which also collapses whichever sibling section was previously open. */
    private fun FlowContent.sidebarEntry(entry: SidebarEntry, currentId: String, depth: Int) {
        when (entry) {
            is SidebarEntry.Item -> {
                val href = resolveHref(entry.href)
                sidebarLink(entry.text, href, active = href == currentId, depth = depth)
            }
            is SidebarEntry.Group -> {
                val open = containsActive(entry, currentId)
                div(classes = "docs-nav-group" + if (open) " open" else "") {
                    if (entry.href != null) {
                        // A linked group heading (e.g. "Home") stays two controls: the title
                        // navigates, and only the chevron toggles — the two can't be merged into
                        // one clickable header without losing one of those actions.
                        div(classes = "docs-nav-header") {
                            val href = resolveHref(entry.href)
                            val active = href == currentId
                            a(href = href, classes = rowClasses(depth, active)) { +entry.title }
                            button(type = ButtonType.button, classes = "docs-nav-expand") {
                                attributes["aria-expanded"] = open.toString()
                                attributes["aria-label"] = "Toggle ${entry.title} section"
                                attributes["onclick"] = "voidToggleDocsSection(this)"
                                icon(Icons.CHEVRON_RIGHT, size = 12)
                            }
                        }
                    } else {
                        // A plain section label (e.g. "Players") has nothing else to click, so the
                        // whole row is one <button> — the title toggles the section just as well as
                        // the chevron does, instead of only the chevron being clickable.
                        button(type = ButtonType.button, classes = rowClasses(depth, extra = "docs-nav-expand")) {
                            attributes["aria-expanded"] = open.toString()
                            attributes["onclick"] = "voidToggleDocsSection(this)"
                            span(classes = "docs-nav-label") { +entry.title }
                            icon(Icons.CHEVRON_RIGHT, size = 12)
                        }
                    }
                    div(classes = "docs-nav-children") {
                        if (!open) {
                            attributes["hidden"] = ""
                        }
                        for (child in entry.children) {
                            sidebarEntry(child, currentId, depth + 1)
                        }
                    }
                }
            }
        }
    }

    private fun docPage(
        source: DocSource,
        all: List<DocSource>,
        sidebar: List<SidebarEntry>?,
        doc: MarkdownDocument,
        previous: DocSource?,
        next: DocSource?,
    ): String = voidPage(
        title = "Void docs — ${source.title}",
        description = source.description,
        assetPrefix = "../",
        head = {
            link(rel = "stylesheet", href = "../style/docs.css")
            script(src = "../js/docs.js") {}
        },
    ) {
        ui.siteHeader(Website.pages, active = "docs", assetPrefix = "../", communityPages = Website.communityPages)

        div {
            attributes["class"] = "docs-layout"
            style = "display:grid;grid-template-columns:240px minmax(0,1fr) 220px;flex:1;" +
                "max-width:var(--container-wide);margin:0 auto;width:100%"

            aside {
                attributes["class"] = "docs-sidebar"
                style = "background:var(--surface-inset);border-right:1px solid var(--border-panel);" +
                    "padding:var(--space-8) var(--space-6);display:flex;flex-direction:column;gap:var(--space-6)"

                div(classes = "docs-search") {
                    div(classes = "docs-search-input-frame") {
                        span {
                            style = "color:var(--text-faint);display:flex"
                            icon(Icons.SEARCH, size = 14)
                        }
                        input(type = InputType.search, classes = "docs-search-input") {
                            attributes["id"] = "docs-search-input"
                            placeholder = "Search docs…"
                            attributes["autocomplete"] = "off"
                            attributes["aria-label"] = "Search docs"
                        }
                    }
                    div {
                        attributes["id"] = "docs-search-results"
                        attributes["class"] = "docs-search-results"
                        attributes["hidden"] = ""
                    }
                }

                nav {
                    style = "display:flex;flex-direction:column;gap:var(--space-3)"
                    if (sidebar != null) {
                        for (entry in sidebar) {
                            sidebarEntry(entry, source.id, depth = 0)
                        }
                    } else {
                        span {
                            style = "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                                "text-transform:uppercase;color:var(--gold-300);margin-bottom:var(--space-2)"
                            +"Reference"
                        }
                        for (item in all) {
                            sidebarLink(item.title, "${item.id}.html", active = item.id == source.id, depth = 0)
                        }
                    }
                }
            }

            article {
                attributes["class"] = "docs-article"
                style = "padding:var(--space-10);min-width:0;max-width:var(--container-body)"
                div {
                    style = "display:flex;align-items:center;gap:var(--space-4);font:var(--type-label);" +
                        "letter-spacing:var(--tracking-wide);color:var(--text-faint);margin-bottom:var(--space-6)"
                    +"Docs"
                    icon(Icons.CHEVRON_RIGHT, size = 12)
                    span { style = "color:var(--parch-200)"; +source.title }
                }
                h1 {
                    style = "margin:0 0 var(--space-6);font:var(--type-title);color:var(--parch-50)"
                    +source.title
                }
                div(classes = "markdown-body") {
                    unsafe { raw(doc.html) }
                }
                if (previous != null || next != null) {
                    div {
                        style = "display:flex;justify-content:space-between;gap:var(--space-5);flex-wrap:wrap;" +
                            "padding-top:var(--space-6);margin-top:var(--space-8);border-top:1px solid var(--border-subtle)"
                        if (previous != null) {
                            ui.button(
                                "← ${previous.title}",
                                variant = ButtonVariant.Secondary,
                                onClick = "window.location = '${previous.id}.html'",
                            )
                        } else {
                            span {}
                        }
                        if (next != null) {
                            ui.button(
                                "${next.title} →",
                                variant = ButtonVariant.Secondary,
                                onClick = "window.location = '${next.id}.html'",
                            )
                        }
                    }
                }
            }

            aside {
                attributes["class"] = "docs-toc"
                style = "padding:var(--space-10) var(--space-6);border-left:1px solid var(--border-panel)"
                val onPage = doc.headings.filter { it.level in 2..3 }
                if (onPage.isNotEmpty()) {
                    div {
                        style = "position:sticky;top:calc(56px + var(--space-6));display:flex;flex-direction:column;gap:var(--space-4)"
                        span {
                            style = "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                                "text-transform:uppercase;color:var(--gold-300)"
                            +"On this page"
                        }
                        for (heading in onPage) {
                            a(href = "#${heading.id}") {
                                val indent = if (heading.level == 3) "12px" else "0"
                                style = "font:var(--type-body-sm);text-decoration:none;color:var(--text-muted);" +
                                    "padding-left:$indent"
                                +heading.text
                            }
                        }
                    }
                }
            }
        }

        ui.siteFooter(assetPrefix = "../")
    }

    private val htmlTag = Regex("<[^>]+>")
    private val whitespace = Regex("\\s+")

    /** Strips markup down to plain text for [indexEntryJson]'s search snippet. */
    private fun stripHtml(html: String): String {
        val text = htmlTag.replace(html, " ")
            .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
            .replace("&quot;", "\"").replace("&#39;", "'").replace("&nbsp;", " ")
        return whitespace.replace(text, " ").trim()
    }

    private fun jsonString(value: String): String {
        val sb = StringBuilder(value.length + 2)
        sb.append('"')
        for (c in value) {
            when (c) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> {}
                '\t' -> sb.append("\\t")
                else -> if (c.code < 0x20) sb.append("\\u%04x".format(c.code)) else sb.append(c)
            }
        }
        sb.append('"')
        return sb.toString()
    }

    /** One `search-index.json` entry `docs.js` fetches client-side for the sidebar search box. */
    private fun indexEntryJson(source: DocSource, doc: MarkdownDocument): String {
        val text = stripHtml(doc.html).take(500)
        return "{\"id\":${jsonString(source.id)},\"title\":${jsonString(source.title)}," +
            "\"description\":${jsonString(source.description)},\"text\":${jsonString(text)}}"
    }
}

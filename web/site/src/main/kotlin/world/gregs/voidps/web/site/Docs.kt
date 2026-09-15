package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.web.site.components.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates the docs section from the Markdown files in `.claude/` (the same reference
 * material [CLAUDE.md]'s "Further Reading" list points at), so the two never drift apart.
 * Each source becomes its own static page — sidebar nav, breadcrumb, "on this page" outline —
 * built entirely from [Ui] components and [SiteChrome], with the Markdown body dropped in as
 * a `.markdown-body`-scoped fragment styled by the same design tokens as the rest of the site.
 */
object Docs {

    private data class DocSource(val id: String, val title: String, val description: String, val file: File)

    private val pages = listOf(
        SitePage("home", "Home", "../index.html"),
        SitePage("docs", "Docs", "index.html"),
        SitePage("play", "Play", "../play.html"),
    )

    private val communityPages = listOf(
        SitePage("hiscores", "Hiscores", "../hiscores.html"),
        SitePage("exchange", "Exchange", "../exchange.html"),
        SitePage("log", "Log", "../log.html"),
    )

    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)

    private val wikiDir = File("../void-wiki/")

    fun generate(buildDir: File) {
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

        val available = sources.filter { it.file.exists() }
        val ids = available.mapTo(mutableSetOf()) { it.id }
        for ((index, source) in available.withIndex()) {
            val (_, body) = extractFrontMatter(source.file.readText())
            val markdown = renderMarkdown(body)
            val previous = available.getOrNull(index - 1)
            val next = available.getOrNull(index + 1)
            val html = docPage(source, available, sidebar, markdown, previous, next)
            File(docsDir, "${source.id}.html").writeText(rewriteDocLinks(html, ids))
        }
        available.firstOrNull()?.let {
            File(docsDir, "index.html").writeText(File(docsDir, "${it.id}.html").readText())
        }
    }

    /** Resolves a sidebar/content link's `href` relative to `docs/`; external links pass through. */
    private fun resolveHref(href: String): String =
        if (href.startsWith("http://") || href.startsWith("https://")) href else href.removePrefix("./")

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

    private fun NAV.sidebarLink(text: String, href: String, active: Boolean, depth: Int) {
        a(href = href) {
            val background = if (active) "var(--surface-active)" else "transparent"
            val border = if (active) "var(--gold-400)" else "transparent"
            val color = if (active) "var(--parch-50)" else "var(--text-muted)"
            val indent = 10 + depth * 14
            style = "text-align:left;padding:var(--space-3) 10px var(--space-3) ${indent}px;" +
                "background:$background;border-left:2px solid $border;border-radius:var(--radius-xs);" +
                "text-decoration:none;font:var(--type-body-sm);color:$color;display:block"
            +text
        }
    }

    private fun NAV.sidebarGroupLabel(text: String, depth: Int) {
        span {
            val indent = 10 + depth * 14
            style = "display:block;padding:var(--space-2) 10px 0 ${indent}px;font:var(--type-label);" +
                "letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--gold-300)"
            +text
        }
    }

    /** Renders one `_Sidebar.md` entry, recursing into nested groups with increasing indent. */
    private fun NAV.sidebarEntry(entry: SidebarEntry, currentId: String, depth: Int) {
        when (entry) {
            is SidebarEntry.Item -> {
                val href = resolveHref(entry.href)
                sidebarLink(entry.text, href, active = href == currentId, depth = depth)
            }
            is SidebarEntry.Group -> {
                if (entry.href != null) {
                    val href = resolveHref(entry.href)
                    sidebarLink(entry.title, href, active = href == currentId, depth = depth)
                } else {
                    sidebarGroupLabel(entry.title, depth)
                }
                for (child in entry.children) {
                    sidebarEntry(child, currentId, depth + 1)
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
        head = { link(rel = "stylesheet", href = "../void/docs.css") },
    ) {
        ui.siteHeader(pages, active = "docs", assetPrefix = "../", communityPages = communityPages)

        div {
            attributes["class"] = "docs-layout"
            style = "display:grid;grid-template-columns:240px minmax(0,1fr) 220px;flex:1;" +
                "max-width:var(--container-wide);margin:0 auto;width:100%"

            aside {
                attributes["class"] = "docs-sidebar"
                style = "background:var(--surface-inset);border-right:1px solid var(--border-panel);" +
                    "padding:var(--space-8) var(--space-6);display:flex;flex-direction:column;gap:var(--space-8)"
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
                div {
                    style = "display:flex;gap:var(--space-4);margin-bottom:var(--space-8)"
                    ui.badge("Reference", pill = false)
                    ui.badge("Updated ${dateFormat.format(Date(source.file.lastModified()))}", tone = BadgeTone.Gold)
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
}

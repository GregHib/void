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

    private val sources = listOf(
        DocSource("build", "Build", "Build and tests", File(".claude/build.md")),
        DocSource("modules", "Modules", "Module layout and dependency direction", File(".claude/modules.md")),
        DocSource("architecture", "Architecture", "Content scripts, events, DI, game loop, inventory", File(".claude/architecture.md")),
        DocSource("testing", "Testing", "WorldTest, integration vs unit patterns", File(".claude/testing.md")),
        DocSource("code-style", "Code Style", "ktlint, structural patterns, naming conventions", File(".claude/code-style.md")),
        DocSource("groml", "Groml Config", "Config format spec and examples", File(".claude/groml.md")),
    )

    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)

    fun generate(buildDir: File) {
        val docsDir = File(buildDir, "docs")
        docsDir.mkdirs()

        val available = sources.filter { it.file.exists() }
        for ((index, source) in available.withIndex()) {
            val markdown = renderMarkdown(source.file.readText())
            val previous = available.getOrNull(index - 1)
            val next = available.getOrNull(index + 1)
            val html = docPage(source, available, markdown, previous, next)
            File(docsDir, "${source.id}.html").writeText(html)
        }
        available.firstOrNull()?.let {
            File(docsDir, "index.html").writeText(File(docsDir, "${it.id}.html").readText())
        }
    }

    private fun docPage(
        source: DocSource,
        all: List<DocSource>,
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
                    span {
                        style = "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                            "text-transform:uppercase;color:var(--gold-300);margin-bottom:var(--space-2)"
                        +"Reference"
                    }
                    for (item in all) {
                        val on = item.id == source.id
                        a(href = "${item.id}.html") {
                            val background = if (on) "var(--surface-active)" else "transparent"
                            val border = if (on) "var(--gold-400)" else "transparent"
                            val color = if (on) "var(--parch-50)" else "var(--text-muted)"
                            style = "text-align:left;padding:var(--space-3) 10px;background:$background;" +
                                "border-left:2px solid $border;border-radius:var(--radius-xs);" +
                                "text-decoration:none;font:var(--type-body-sm);color:$color;display:block"
                            +item.title
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

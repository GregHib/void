package world.gregs.voidps.web.site.components

import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.footer
import kotlinx.html.header
import kotlinx.html.img
import kotlinx.html.nav
import kotlinx.html.span
import kotlinx.html.style

data class SitePage(val id: String, val label: String, val href: String)

/**
 * The marketing site's fixed top bar: mark + wordmark, a page-to-page nav (plain links, since
 * each page is its own static file rather than an Alpine tab), and a right-hand slot for the
 * version badge, source link and account action. [active] is the current page's [SitePage.id].
 */
fun Ui.siteHeader(pages: List<SitePage>, active: String, assetPrefix: String = "", worlds: List<WorldEntry> = defaultWorlds) {
    receiver.header {
        style = "height:56px;display:flex;align-items:stretch;gap:var(--space-8);padding:0 var(--space-7);" +
            "background:var(--surface-header);border-bottom:1px solid var(--border-gold);" +
            "box-shadow:var(--shadow-sm);position:sticky;top:0;z-index:30"
        a(href = "${assetPrefix}index.html") {
            style = "display:flex;align-items:center;gap:10px;flex:0 0 auto;text-decoration:none"
            img(src = "${assetPrefix}void/void-mark-glyph-gold.svg", alt = "Void") {
                style = "width:26px;height:26px;display:block"
            }
            span {
                style = "font:var(--weight-bold) var(--text-lg)/1 var(--font-display);" +
                    "letter-spacing:var(--tracking-caps);color:var(--parch-50)"
                +"VOID"
            }
        }
        nav {
            style = "display:flex;align-items:stretch;gap:var(--space-2);flex:1;min-width:0;overflow:hidden"
            for (page in pages) {
                val on = page.id == active
                a(href = page.href) {
                    val color = if (on) "var(--gold-300)" else "var(--text-muted)"
                    val underline = if (on) "var(--gold-400)" else "transparent"
                    style = "display:inline-flex;align-items:center;padding:0 14px;color:$color;" +
                        "border-bottom:2px solid $underline;text-decoration:none;" +
                        "font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);letter-spacing:var(--tracking-wide);" +
                        "margin-bottom:-1px;transition:color var(--dur-fast) var(--ease-standard)"
                    +page.label
                }
            }
        }
        div {
            style = "display:flex;align-items:center;gap:var(--space-6);flex:0 0 auto"
            ui.badge("v0.41.2", pill = false)
            a(href = "#") {
                style = "display:inline-flex;align-items:center;gap:var(--space-3);font:var(--type-body-sm)"
                icon(Icons.EXTERNAL, size = 14)
                +"Source"
            }
            ui.worldMenu(worlds, worldsHref = "${assetPrefix}worlds.html")
            ui.accountMenu(name = "rotce", isAdmin = true, devPanelHref = "${assetPrefix}dev/index.html")
        }
    }
}

/** Four link columns, the mark, and the build-stamp/not-affiliated line every Void surface carries. */
fun Ui.siteFooter(assetPrefix: String = "") {
    val columns = listOf(
        "Project" to listOf("About", "Roadmap", "Changelog", "Licence"),
        "Developers" to listOf("Getting started", "Protocol reference", "Cache tooling", "Contributing"),
        "Play" to listOf("Download launcher", "World list", "Account", "Membership"),
        "Community" to listOf("Discord", "Forums", "Wiki", "Bug tracker"),
    )
    receiver.footer {
        style = "margin-top:auto;border-top:1px solid var(--border-panel);background:var(--surface-inset);" +
            "padding:var(--space-11) var(--space-8) var(--space-8)"
        div {
            style = "max-width:var(--container-wide);margin:0 auto;display:grid;" +
                "grid-template-columns:1.4fr repeat(4,1fr);gap:var(--space-9)"
            div {
                style = "display:flex;flex-direction:column;gap:var(--space-5)"
                div {
                    style = "display:flex;align-items:center;gap:10px"
                    img(src = "${assetPrefix}void/void-mark-tile-gold.svg", alt = "") {
                        style = "width:26px;height:26px;display:block"
                    }
                    span {
                        style = "font:var(--weight-bold) var(--text-lg)/1 var(--font-display);" +
                            "letter-spacing:var(--tracking-caps);color:var(--parch-100)"
                        +"VOID"
                    }
                }
                span {
                    style = "font:var(--type-body-sm);color:var(--text-faint);max-width:280px"
                    +"Modern mmo emulation. Open source, run by the people who play it."
                }
            }
            for ((heading, items) in columns) {
                nav {
                    style = "display:flex;flex-direction:column;gap:var(--space-4)"
                    span {
                        style = "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                            "text-transform:uppercase;color:var(--gold-300)"
                        +heading
                    }
                    for (item in items) {
                        a(href = "#") {
                            style = "font:var(--type-body-sm);color:var(--text-muted);text-decoration:none"
                            +item
                        }
                    }
                }
            }
        }
        div {
            style = "max-width:var(--container-wide);margin:var(--space-9) auto 0;padding-top:var(--space-6);" +
                "border-top:1px solid var(--border-subtle);display:flex;justify-content:space-between;gap:var(--space-6);" +
                "font:var(--type-label);letter-spacing:var(--tracking-wide);color:var(--text-faint)"
            span { +"Void is an independent emulation project. Not affiliated with any game publisher." }
            span {
                style = "font:var(--type-code);font-size:var(--text-2xs)"
                +"9f2ac31 · built 8 Sept 2026"
            }
        }
    }
}

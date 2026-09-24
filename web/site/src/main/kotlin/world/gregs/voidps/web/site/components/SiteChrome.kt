package world.gregs.voidps.web.site.components

import kotlinx.html.*
import world.gregs.voidps.web.site.Site
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class SitePage(val id: String, val label: String, val href: String)

/**
 * Resolves a [SitePage.href]/footer link against [assetPrefix]. A leading `/` marks a path as
 * relative to the *site root* rather than the current page's directory — since GitHub Pages
 * project sites are served from a subpath (`<user>.github.io/<repo>/`), an actual root-absolute
 * `href="/…"` would instead resolve against the domain root and drop that subpath. Swapping the
 * marker for [assetPrefix] keeps the link relative, so it survives being served from any subpath.
 * Anything else (a plain relative path, or an external `http(s)://` link) passes through as-is.
 */
private fun siteHref(href: String, assetPrefix: String): String =
    if (href.startsWith("/")) "$assetPrefix${href.removePrefix("/")}" else href

/**
 * The site's fixed top bar: mark + wordmark, a page-to-page nav (plain links, since each page is
 * its own static file rather than an Alpine tab), and a right-hand slot for the version badge,
 * source link and account action. [active] is the current page's [SitePage.id].
 *
 * Also backs the staff-only developer panel ([world.gregs.voidps.web.site.Dev]) — pass
 * [panelTag] for the "DEV PANEL" mark next to the wordmark, and [worldLabel] to swap the version
 * badge/source link/world menu for a target-world readout with an optional [liveModel]-backed
 * live/paused switch (`null` omits the switch on pages whose data model has no such property).
 * [devPanelHref] overrides where [accountMenu]'s "Developer panel" entry points — pass the
 * panel's own first page when already inside it, since the default assumes a marketing page.
 */
fun Ui.siteHeader(
    pages: List<SitePage>,
    active: String,
    assetPrefix: String = "",
    worlds: List<WorldEntry> = defaultWorlds,
    communityPages: List<SitePage> = emptyList(),
    panelTag: String? = null,
    worldLabel: String? = null,
    liveModel: String? = null,
    devPanelHref: String = "${assetPrefix}dev/index.html",
) {
    receiver.header {
        attributes["class"] = "void-header"
        xData("{ mobileOpen: false }")
        onClickOutside("mobileOpen = false")
        style = "height:56px;display:flex;align-items:stretch;gap:var(--space-8);padding:0 var(--space-7);" +
            "background:var(--surface-header);border-bottom:1px solid var(--border-gold);" +
            "box-shadow:var(--shadow-sm);position:sticky;top:0;z-index:30"
        a(href = "${assetPrefix}index.html") {
            style = "display:flex;align-items:center;gap:10px;flex:0 0 auto;text-decoration:none"
            img(src = "${assetPrefix}void-mark-glyph-gold.svg", alt = "Void") {
                style = "width:26px;height:26px;display:block"
            }
            span {
                style = "font:var(--weight-bold) var(--text-lg)/1 var(--font-display);" +
                    "letter-spacing:var(--tracking-caps);color:var(--parch-50)"
                +"VOID"
            }
            if (panelTag != null) {
                span {
                    style = "font:var(--weight-medium) var(--text-2xs)/1 var(--font-ui);" +
                        "letter-spacing:var(--tracking-caps);color:var(--text-faint);padding-left:var(--space-1)"
                    +panelTag
                }
            }
        }
        nav {
            attributes["class"] = "void-header-nav"
            xBindClass("mobileOpen ? 'void-nav-open' : ''")
            style = "display:flex;align-items:stretch;gap:var(--space-2);flex:1;min-width:0"
            for (page in pages) {
                val on = page.id == active
                a(href = siteHref(page.href, assetPrefix)) {
                    val color = if (on) "var(--gold-300)" else "var(--text-muted)"
                    val underline = if (on) "var(--gold-400)" else "transparent"
                    style = "display:inline-flex;align-items:center;padding:0 14px;color:$color;" +
                        "border-bottom:2px solid $underline;text-decoration:none;" +
                        "font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);letter-spacing:var(--tracking-wide);" +
                        "margin-bottom:-1px;transition:color var(--dur-fast) var(--ease-standard)"
                    +page.label
                }
            }
            if (communityPages.isNotEmpty()) {
                val on = communityPages.any { it.id == active }
                div {
                    attributes["class"] = "void-header-community"
                    xData("{ open: false }")
                    onClickOutside("open = false")
                    onMouseEnter("open = true")
                    onMouseLeave("open = false")
                    style = "position:relative;display:flex;align-items:stretch;flex:0 0 auto"
                    button {
                        onClick("open = !open")
                        val color = if (on) "var(--gold-300)" else "var(--text-muted)"
                        val underline = if (on) "var(--gold-400)" else "transparent"
                        style = "display:inline-flex;align-items:center;gap:var(--space-2);padding:0 14px;" +
                            "color:$color;background:transparent;border:none;border-bottom:2px solid $underline;" +
                            "font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);letter-spacing:var(--tracking-wide);" +
                            "margin-bottom:-1px;cursor:pointer;transition:color var(--dur-fast) var(--ease-standard)"
                        +"Community"
                        icon(Icons.CHEVRON_DOWN, size = 11)
                    }
                    div {
                        attributes["class"] = "void-header-community-panel"
                        xShow("open")
                        transition()
                        onClickStop("null")
                        // Flush against the header (no gap below the trigger) so the pointer never
                        // crosses empty space on the way down — a gap there was closing the menu
                        // (mouseleave firing) before the cursor reached it.
                        style = "position:absolute;top:100%;left:0;width:220px;" +
                            "background:var(--surface-panel);border:1px solid var(--border-gold);" +
                            "border-top:none;border-radius:0 0 var(--radius-md) var(--radius-md);" +
                            "box-shadow:var(--bevel-up),var(--shadow-lg);overflow:hidden;z-index:40"
                        for (page in communityPages) {
                            val itemOn = page.id == active
                            a(href = siteHref(page.href, assetPrefix), classes = "void-menu-item") {
                                val itemColor = if (itemOn) "var(--gold-300)" else "var(--text-body)"
                                val itemBackground = if (itemOn) "background:var(--surface-active);" else ""
                                style = "display:block;padding:var(--space-5) var(--space-6);" +
                                    "text-decoration:none;color:$itemColor;$itemBackground" +
                                    "font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);" +
                                    "transition:background var(--dur-fast) var(--ease-standard)"
                                +page.label
                            }
                        }
                    }
                }
            }
            div {
                attributes["class"] = "void-header-nav-end"
                style = "display:flex;align-items:center;gap:var(--space-6);flex:0 0 auto;margin-left:auto"
                if (worldLabel != null) {
                    span {
                        style = "font:var(--type-code);font-size:var(--text-xs);color:var(--text-muted)"
                        +worldLabel
                    }
                    if (liveModel != null) {
                        span { style = "width:1px;height:22px;background:var(--border-subtle)" }
                        ui.switch("Live", model = liveModel, small = true)
                    }
                    span { style = "width:1px;height:22px;background:var(--border-subtle)" }
                    ui.accountMenu(name = "rotce", isAdmin = true, devPanelHref = devPanelHref)
                } else {
                    div {
                        attributes["class"] = "void-header-extra"
                        style = "display:flex;align-items:center;gap:var(--space-6)"
                        ui.badge(Site.version, pill = false)
                        a(href = "https://github.com/GregHib/void") {
                            style = "display:inline-flex;align-items:center;gap:var(--space-3);font:var(--type-body-sm)"
                            icon(Icons.EXTERNAL, size = 14)
                            +"Source"
                        }
                    }
                    ui.worldMenu(worlds, worldsHref = "${assetPrefix}worlds.html")
                    if (Site.FULL) {
                        ui.accountMenu(name = "rotce", isAdmin = true, devPanelHref = devPanelHref)
                    }
                }
            }
        }
        button {
            attributes["class"] = "void-header-toggle"
            attributes["aria-label"] = "Toggle menu"
            onClick("mobileOpen = !mobileOpen")
            style = "display:none;align-items:center;justify-content:center;width:36px;height:36px;flex:0 0 auto;" +
                "align-self:center;margin-left:auto;background:transparent;border:none;color:var(--text-muted);cursor:pointer"
            icon(Icons.MENU, size = 20)
        }
    }
}

/** Four link columns, the mark, and the build-stamp/not-affiliated line every Void surface carries. */
fun Ui.siteFooter(assetPrefix: String = "") {
    val columns = listOf(
        "Project" to listOf(Pair("About", "/index.html"), Pair("Roadmap", "/docs/roadmap.html"), Pair("Changelog", "https://github.com/GregHib/void/releases"), Pair("Licence", "https://github.com/GregHib/void/blob/main/LICENSE")),
        "Developers" to listOf(Pair("Getting started", "/docs/content-creation.html"), Pair("Contributing", "https://github.com/GregHib/void/blob/main/CONTRIBUTING.md")),
        "Play" to listOf(Pair("Download", "https://github.com/GregHib/void/releases"), Pair("Install Guide", "/docs/installation-guide.html")),
        "Community" to listOf(Pair("Bug tracker", "https://github.com/GregHib/void/issues")),
    )
    receiver.footer {
        style = "margin-top:auto;border-top:1px solid var(--border-panel);background:var(--surface-inset);" +
            "padding:var(--space-11) var(--space-8) var(--space-8)"
        div {
            attributes["class"] = "void-footer-columns"
            style = "max-width:var(--container-wide);margin:0 auto;display:grid;" +
                "grid-template-columns:1.4fr repeat(4,1fr);gap:var(--space-9)"
            div {
                style = "display:flex;flex-direction:column;gap:var(--space-5)"
                div {
                    style = "display:flex;align-items:center;gap:10px"
                    img(src = "${assetPrefix}void-mark-tile-gold.svg", alt = "") {
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
                    +"Void is an unofficial open-source fan project made for archival and educational purposes only. All trademarks and copyrights remain property of their respective owners."
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
                    for ((item, ref) in items) {
                        a(href = siteHref(ref, assetPrefix)) {
                            style = "font:var(--type-body-sm);color:var(--text-muted);text-decoration:none"
                            +item
                        }
                    }
                }
            }
        }
        div {
            attributes["class"] = "void-footer-bottom"
            style = "max-width:var(--container-wide);margin:var(--space-9) auto 0;padding-top:var(--space-6);" +
                "border-top:1px solid var(--border-subtle);display:flex;justify-content:space-between;gap:var(--space-6);" +
                "font:var(--type-label);letter-spacing:var(--tracking-wide);color:var(--text-faint)"
            span { +"Void is an independent emulation project. Not affiliated with any game publisher." }
            span {
                val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
                    .withZone(ZoneId.systemDefault())
                style = "font:var(--type-code);font-size:var(--text-2xs)"
                +"built ${formatter.format(Instant.now())}"
            }
        }
    }
}

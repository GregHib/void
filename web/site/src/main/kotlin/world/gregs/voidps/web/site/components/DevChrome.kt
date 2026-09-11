package world.gregs.voidps.web.site.components

import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.header
import kotlinx.html.img
import kotlinx.html.nav
import kotlinx.html.span
import kotlinx.html.style

/**
 * The developer panel's fixed top bar: mark + "DEV PANEL" tag, a page-to-page nav (plain links,
 * one static file per page — see [siteHeader]), and a right-hand slot for the target world, a
 * live/paused switch and the staff account menu. [active] is the current page's [SitePage.id];
 * [homeHref] points back at the public site's home page. [liveModel] names the boolean Alpine
 * property backing the live/paused switch — pass `null` on a page whose data model has no such
 * property (the switch is omitted rather than binding to an undefined variable).
 */
fun Ui.devHeader(
    pages: List<SitePage>,
    active: String,
    worldLabel: String = "World 9 · voidmmo-eu-1 · rev 231",
    liveModel: String? = "live",
    homeHref: String = "../index.html",
    assetPrefix: String = "",
) {
    receiver.header {
        attributes["class"] = "void-header"
        xData("{ mobileOpen: false }")
        onClickOutside("mobileOpen = false")
        style = "height:56px;display:flex;align-items:stretch;gap:var(--space-8);padding:0 var(--space-7);" +
            "background:var(--surface-header);border-bottom:1px solid var(--border-gold);" +
            "box-shadow:var(--shadow-sm);position:sticky;top:0;z-index:30"
        a(href = homeHref) {
            style = "display:flex;align-items:center;gap:10px;flex:0 0 auto;text-decoration:none"
            img(src = "${assetPrefix}void/void-mark-glyph-gold.svg", alt = "Void") {
                style = "width:26px;height:26px;display:block"
            }
            span {
                style = "font:var(--weight-bold) var(--text-lg)/1 var(--font-display);" +
                    "letter-spacing:var(--tracking-caps);color:var(--parch-50)"
                +"VOID"
            }
            span {
                style = "font:var(--weight-medium) var(--text-2xs)/1 var(--font-ui);" +
                    "letter-spacing:var(--tracking-caps);color:var(--text-faint);padding-left:var(--space-1)"
                +"DEV PANEL"
            }
        }
        nav {
            attributes["class"] = "void-header-nav"
            xBindClass("mobileOpen ? 'void-nav-open' : ''")
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
            div {
                attributes["class"] = "void-header-nav-end"
                style = "display:flex;align-items:center;gap:var(--space-6);flex:0 0 auto;margin-left:auto"
                span {
                    style = "font:var(--type-code);font-size:var(--text-xs);color:var(--text-muted)"
                    +worldLabel
                }
                if (liveModel != null) {
                    span { style = "width:1px;height:22px;background:var(--border-subtle)" }
                    ui.switch("Live", model = liveModel, small = true)
                }
                span { style = "width:1px;height:22px;background:var(--border-subtle)" }
                ui.accountMenu(name = "rotce", isAdmin = true, devPanelHref = pages.first().href)
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

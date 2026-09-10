package world.gregs.voidps.web.site.components

import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.style

/**
 * The navbar's account slot. Renders a "Log in" button that, on click, simulates authenticating
 * and swaps itself for a round account icon; clicking the icon opens a small dropdown with the
 * account name/email, account-management links and a logout action that reverts the swap. All
 * state (`loggedIn`, `open`) is local to this component's own `x-data` scope, so it can be dropped
 * into [menuBar] or [siteHeader]'s `right` slot without any page-level wiring.
 *
 * [isAdmin] adds a "Developer panel" entry above the divider, linking to [devPanelHref], for
 * staff accounts only.
 */
fun Ui.accountMenu(
    name: String = "Zezima",
    email: String = "zezima@voidps.dev",
    isAdmin: Boolean = false,
    devPanelHref: String = "dev/index.html",
) {
    receiver.div {
        xData("{ loggedIn: false, open: false }")
        onClickOutside("open = false")
        style = "position:relative;flex:0 0 auto"

        // Logged-out state: plain login button.
        button {
            xShow("!loggedIn")
            onClick("loggedIn = true")
            attributes["class"] = "void-btn void-btn-secondary"
            style = "display:inline-flex;align-items:center;justify-content:center;height:28px;" +
                "padding:0 12px;border-radius:var(--radius-md);" +
                "font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);letter-spacing:0.06em;cursor:pointer"
            +"Log in"
        }

        // Logged-in state: round account icon that opens the dropdown.
        button {
            xShow("loggedIn")
            attributes["aria-label"] = "Account menu"
            onClick("open = !open")
            xToggleStyle(
                condition = "open",
                whenTrue = "background:var(--surface-active);border-color:var(--gold-500)",
                whenFalse = "background:var(--surface-inset);border-color:var(--border-strong)",
            )
            style = "width:32px;height:32px;display:inline-flex;align-items:center;justify-content:center;" +
                "border-radius:50%;border:1px solid var(--border-strong);color:var(--gold-300);cursor:pointer;" +
                "transition:background var(--dur-fast) var(--ease-standard)"
            icon(Icons.ACCOUNT, size = 18)
        }

        div {
            xShow("open")
            transition()
            onClickStop("null")
            style = "position:absolute;top:calc(100% + 10px);right:0;width:220px;" +
                "background:var(--surface-panel);border:1px solid var(--border-gold);" +
                "border-radius:var(--radius-md);box-shadow:var(--bevel-up),var(--shadow-lg);" +
                "overflow:hidden;z-index:40"

            div {
                style = "display:flex;flex-direction:column;gap:2px;padding:var(--space-5) var(--space-6);" +
                    "background:var(--surface-header);border-bottom:1px solid var(--border-gold)"
                span {
                    style = "font:var(--weight-semibold) var(--text-sm)/1.3 var(--font-ui);color:var(--parch-50)"
                    +name
                }
                span {
                    style = "font:var(--type-label);letter-spacing:var(--tracking-wide);color:var(--text-faint)"
                    +email
                }
            }

            div {
                style = "display:flex;flex-direction:column;padding:var(--space-3)"
                accountMenuItem(Icons.ACCOUNT, "Account management")
                accountMenuItem(Icons.SETTINGS, "Settings")
                if (isAdmin) {
                    accountMenuItem(Icons.SHIELD, "Developer panel", href = devPanelHref)
                }
            }

            div {
                style = "padding:var(--space-3);border-top:1px solid var(--border-subtle)"
                button {
                    onClick("loggedIn = false; open = false")
                    style = "width:100%;display:flex;align-items:center;gap:10px;padding:8px 10px;" +
                        "background:transparent;border:none;border-radius:var(--radius-xs);" +
                        "color:var(--feedback-danger);cursor:pointer;text-align:left;" +
                        "font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);" +
                        "transition:background var(--dur-fast) var(--ease-standard)"
                    icon(Icons.LOGOUT, size = 15)
                    +"Log out"
                }
            }
        }
    }
}

private fun kotlinx.html.DIV.accountMenuItem(iconPath: String, label: String, href: String = "#") {
    a(href = href) {
        style = "display:flex;align-items:center;gap:10px;padding:8px 10px;border-radius:var(--radius-xs);" +
            "color:var(--text-body);text-decoration:none;font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);" +
            "transition:background var(--dur-fast) var(--ease-standard)"
        span { style = "color:var(--text-faint);display:inline-flex"; icon(iconPath, size = 15) }
        +label
    }
}

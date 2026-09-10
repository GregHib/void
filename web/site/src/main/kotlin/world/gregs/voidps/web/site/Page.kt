package world.gregs.voidps.web.site

import kotlinx.html.*
import kotlinx.html.stream.appendHTML

/**
 * Renders a full HTML document using the Void design system: fonts/token stylesheets,
 * Alpine.js and the shared page reset. [data] seeds the root `x-data` state object that
 * every component on the page reads and writes.
 */
fun voidPage(
    title: String,
    description: String? = null,
    assetPrefix: String = "",
    cssPath: String = "${assetPrefix}void/base.css",
    alpineVersion: String = "3.14.1",
    data: String = "{}",
    head: HEAD.() -> Unit = {},
    body: DIV.() -> Unit,
): String = StringBuilder().appendHTML().html {
    lang = "en"
    head {
        meta(charset = "utf-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1")
        title(title)
        if (description != null) {
            meta(name = "description", content = description)
        }
        link(rel = "stylesheet", href = cssPath)
        script(src = "${assetPrefix}void/void.js") {}
        script(src = "https://cdn.jsdelivr.net/npm/alpinejs@$alpineVersion/dist/cdn.min.js") {
            attributes["defer"] = ""
        }
        head()
    }
    body {
        div {
            attributes["x-data"] = data
            style = "min-height:100vh;display:flex;flex-direction:column;background:var(--surface-app);" +
                "font:var(--type-body);color:var(--text-body)"
            body()
        }
    }
}.toString()

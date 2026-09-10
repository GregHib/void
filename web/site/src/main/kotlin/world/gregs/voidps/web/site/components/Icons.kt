package world.gregs.voidps.web.site.components

import kotlinx.html.HTMLTag
import kotlinx.html.unsafe

/**
 * Lucide-style stroke icons (2px stroke, square caps), inlined as raw SVG so pages stay
 * dependency-free. Pass a [Void.Icons] entry to component `icon` parameters, or call
 * [FlowContent.icon] directly.
 */
object Icons {
    const val SEARCH = """<circle cx="11" cy="11" r="7"></circle><path d="M16.5 16.5 21 21"></path>"""
    const val SETTINGS = """<circle cx="12" cy="12" r="3"></circle><path d="M12 2v3M12 19v3M2 12h3M19 12h3M5 5l2 2M17 17l2 2M19 5l-2 2M7 17l-2 2"></path>"""
    const val MENU = """<path d="M4 6h16M4 12h16M4 18h16"></path>"""
    const val PLAY = """<path d="M6 4l14 8-14 8z"></path>"""
    const val DOWNLOAD = """<path d="M12 3v12"></path><path d="M7 11l5 5 5-5"></path><path d="M4 20h16"></path>"""
    const val CLOSE = """<path d="M6 6l12 12M18 6 6 18"></path>"""
    const val CHEVRON_DOWN = """<path d="M6 9l6 6 6-6"></path>"""
    const val CHECK = """<path d="M5 12l5 5 9-9"></path>"""
}

fun HTMLTag.icon(path: String, size: Int = 18) {
    unsafe {
        raw(
            """<svg width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="square" aria-hidden="true">$path</svg>""",
        )
    }
}

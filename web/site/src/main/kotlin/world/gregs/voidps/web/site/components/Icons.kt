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
    const val CHEVRON_RIGHT = """<path d="M9 6l6 6-6 6"></path>"""
    const val CHECK = """<path d="M5 12l5 5 9-9"></path>"""
    const val TERMINAL = """<path d="M4 17l6-5-6-5"></path><path d="M12 19h8"></path>"""
    const val BOOK = """<path d="M4 19.5V5a2 2 0 0 1 2-2h13v15H6a2 2 0 0 0 0 4h13"></path>"""
    const val USERS = """<path d="M17 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path>"""
    const val EXTERNAL = """<path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path><path d="M15 3h6v6"></path><path d="M10 14 21 3"></path>"""
    const val MAIL = """<path d="M4 5h16v14H4z"></path><path d="M4 6l8 7 8-7"></path>"""
    const val ACCOUNT = """<circle cx="12" cy="8" r="4"></circle><path d="M4 20c0-4.4 3.6-7 8-7s8 2.6 8 7"></path>"""
    const val LOGOUT = """<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><path d="M16 17l5-5-5-5"></path><path d="M21 12H9"></path>"""
    const val INFO = """<circle cx="12" cy="12" r="10"></circle><path d="M12 16v-4M12 8h.01"></path>"""
    const val LIGHTBULB = """<path d="M9 18h6M10 22h4"></path><path d="M12 2a7 7 0 0 0-4 12.7c.5.4.8 1 .8 1.7v.6h6.4v-.6c0-.7.3-1.3.8-1.7A7 7 0 0 0 12 2Z"></path>"""
    const val MEGAPHONE = """<path d="M3 11v2a2 2 0 0 0 2 2h1l3 5V4L6 9H5a2 2 0 0 0-2 2Z"></path><path d="M15 8a4 4 0 0 1 0 8"></path><path d="M19 5a9 9 0 0 1 0 14"></path>"""
    const val ALERT_TRIANGLE = """<path d="M12 9v4M12 17h.01"></path><path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z"></path>"""
    const val SHIELD = """<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z"></path>"""
    const val GAUGE = """<circle cx="12" cy="13" r="8"></circle><path d="M12 13l3-3M9 5h6"></path>"""
}

fun HTMLTag.icon(path: String, size: Int = 18) {
    unsafe {
        raw(
            """<svg width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="square" aria-hidden="true">$path</svg>""",
        )
    }
}

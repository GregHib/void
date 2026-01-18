package world.gregs.voidps.website

import io.ktor.server.sessions.*
import kotlinx.html.*
import world.gregs.voidps.website.UserSession

fun HTML.commonLayout(title: String, session: UserSession?, content: FlowContent.() -> Unit) {
    head {
        title("$title - Void")
        script(src = "https://cdn.tailwindcss.com") {}
        style {
            unsafe {
                raw("""
                    .glass {
                        background: rgba(15, 23, 42, 0.8);
                        backdrop-filter: blur(8px);
                        border: 1px solid rgba(255, 255, 255, 0.05);
                    }
                    .nav-link:hover {
                        background: rgba(255, 255, 255, 0.03);
                    }
                """.trimIndent())
            }
        }
    }
    body("bg-slate-950 text-slate-200 font-sans min-h-screen") {
        navbar(session)
        content()
    }
}

fun FlowContent.navbar(session: UserSession?) {
    nav("glass sticky top-0 z-50 px-6 py-3 flex items-center justify-between border-b border-white/5") {
        div("flex items-center gap-8") {
            // Home button/Logo
            a(href = "/", classes = "text-xl font-black tracking-tighter bg-gradient-to-br from-white to-slate-500 bg-clip-text text-transparent hover:opacity-80 transition-opacity") {
                +"VOID"
            }

            // Main Links
            div("flex items-center gap-2") {
                navLink("/highscores", "Highscores")
                navLink("/grand-exchange", "Grand Exchange")
            }
        }

        div("flex items-center gap-4") {
            if (session == null) {
                a(href = "/login", classes = "px-4 py-2 bg-slate-100 hover:bg-white text-slate-900 text-xs font-bold uppercase tracking-wider rounded-sm transition-colors") {
                    +"Login"
                }
            } else {
                div("flex items-center gap-4") {
                    if (session.rights.equals("admin", ignoreCase = true)) {
                        a(href = "/admin", classes = "px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white text-xs font-bold uppercase tracking-wider rounded-sm border border-white/5 transition-colors") {
                            +"Admin Panel"
                        }
                    }
                    div("flex flex-col items-end leading-tight") {
                        span("text-[10px] text-slate-500 uppercase font-black tracking-widest") { +"Logged in as" }
                        span("text-white font-bold text-sm") { +session.username }
                    }
                    a(href = "/logout", classes = "px-3 py-1.5 text-slate-500 hover:text-rose-400 text-[10px] font-bold uppercase tracking-widest transition-colors border border-white/5 hover:border-rose-500/20 rounded-sm") {
                        +"Logout"
                    }
                }
            }
        }
    }
}

fun FlowContent.navLink(url: String, text: String) {
    a(href = url, classes = "nav-link px-3 py-1.5 rounded-sm text-xs font-bold uppercase tracking-wider text-slate-500 hover:text-white transition-all") {
        +text
    }
}

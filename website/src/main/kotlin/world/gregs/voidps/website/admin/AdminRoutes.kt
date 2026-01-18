package world.gregs.voidps.website.admin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import world.gregs.voidps.website.UserSession
import world.gregs.voidps.website.commonLayout
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.command.Commands
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.get
import java.lang.management.ManagementFactory
import java.util.LinkedList
import java.util.Collections

data class TickData(
    val tick: Int,
    val duration: Long,
    val stages: Map<String, Long>,
    val memoryUsed: Long,
    val cpuLoad: Double
)

object AdminContext {
    val tickHistory = Collections.synchronizedList(LinkedList<TickData>())
    val longTermHistory = Collections.synchronizedList(LinkedList<TickData>())
    val adminPlayer = Player(variables = mutableMapOf("rights" to "admin"))

    init {
        GameLoop.tickListeners.add { tick, time, stages ->
            val mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val osBean = ManagementFactory.getOperatingSystemMXBean()
            val load = osBean.systemLoadAverage
            
            val data = TickData(tick, time, stages, mem, load)
            tickHistory.add(data)
            if (tickHistory.size > 100) {
                tickHistory.removeAt(0)
            }

            if (tick % 100 == 0) {
                longTermHistory.add(data)
                if (longTermHistory.size > 1440) { // 24 hours at 1-min intervals
                    longTermHistory.removeAt(0)
                }
            }
        }
    }
}

fun Route.adminRoutes() {
    intercept(ApplicationCallPipeline.Call) {
        val path = call.request.path()
        if (path.startsWith("/admin")) {
            val session = call.sessions.get<UserSession>()
            if (session == null) {
                call.respondRedirect("/login")
                finish()
                return@intercept
            }
            if (!session.rights.equals("admin", ignoreCase = true)) {
                call.respondHtml(HttpStatusCode.Forbidden) {
                    commonLayout("Unauthorized", session) {
                        div("min-h-[calc(100vh-64px)] flex items-center justify-center p-4") {
                            div("max-w-md w-full glass p-12 rounded-sm text-center border border-rose-500/20 shadow-2xl space-y-8") {
                                div("w-20 h-20 bg-rose-500/10 rounded-sm flex items-center justify-center mx-auto border border-rose-500/20") {
                                    span("text-4xl text-rose-500") { +"!" }
                                }
                                div("space-y-2") {
                                    h1("text-3xl font-black text-white uppercase tracking-wider") { +"Access Denied" }
                                    p("text-slate-500 font-medium") { +"You do not have the necessary permissions to access the Admin Panel." }
                                }
                                div("flex flex-col gap-3") {
                                    a(href = "/", classes = "px-8 py-3 bg-white text-slate-900 rounded-sm font-black uppercase text-xs tracking-[0.2em] hover:bg-slate-200 transition-all") { +"Return Home" }
                                }
                            }
                        }
                    }
                }
                finish()
                return@intercept
            }
        }
    }

    get("/admin") {
        val session = call.sessions.get<UserSession>()
        call.respondHtml {
            adminLayout(session) {
                div("grid grid-cols-1 lg:grid-cols-3 gap-4") {
                    div("lg:col-span-2 bg-slate-950 p-6 rounded-sm border border-white/5") {
                        div("flex justify-between items-center mb-4") {
                             h2("text-lg font-black text-white uppercase tracking-widest") { +"Performance" }
                             button(classes = "text-[10px] font-black uppercase tracking-widest bg-slate-900 px-3 py-1 rounded-sm border border-white/5 hover:bg-slate-800 transition-colors") {
                                 id = "pause-btn"
                                 onClick = "togglePause()"
                                 +"Pause"
                             }
                        }
                        
                        div {
                            id = "tick-graph"
                            attributes["hx-get"] = "/admin/ticks"
                            attributes["hx-trigger"] = "every 600ms"
                            attributes["hx-swap"] = "innerHTML"
                            +"Loading graphs..."
                        }

                        div("mt-8 pt-8 border-t border-white/5") {
                            div("flex justify-between items-center mb-4") {
                                h2("text-lg font-black text-white uppercase tracking-widest") { +"Long-term Trends" }
                                span("text-[10px] text-slate-600 font-black uppercase tracking-widest") { +"1 min intervals" }
                            }
                            div {
                                id = "long-term-graph"
                                attributes["hx-get"] = "/admin/long-term"
                                attributes["hx-trigger"] = "every 60s load"
                                attributes["hx-swap"] = "innerHTML"
                                +"Waiting for data..."
                            }
                        }
                    }
                    
                    div("flex flex-col gap-4") {
                        div("bg-slate-950 p-6 rounded-sm border border-white/5") {
                            h2("text-sm font-black text-slate-500 uppercase tracking-widest mb-4") { +"Metrics" }
                             div {
                                 id = "metrics-display"
                                 // Will be populated by /admin/ticks
                                 +"Waiting for data..."
                             }
                        }
                        div("bg-slate-950 p-6 rounded-sm border border-white/5 flex-grow") {
                            h2("text-sm font-black text-slate-500 uppercase tracking-widest mb-4") { +"Game Console" }
                             form {
                                attributes["hx-post"] = "/admin/console"
                                attributes["hx-target"] = "#console-output"
                                attributes["hx-on::after-request"] = "this.reset()"
                                input(type = InputType.text, name = "command", classes = "w-full bg-slate-900 border border-white/5 text-white p-2 rounded-sm mb-2 text-sm focus:outline-none focus:border-blue-500/50") {
                                    placeholder = "::players"
                                }
                            }
                            div("h-48 overflow-y-auto font-mono text-[11px] bg-slate-950 p-3 border border-white/5") {
                                id = "console-output"
                                 +"Ready..."
                            }
                        }
                    }
                }
                div("bg-slate-950 p-6 rounded-sm border border-white/5 mt-4") {
                    h2("text-sm font-black text-slate-500 uppercase tracking-widest mb-4") { +"Online Players" }
                     div {
                            id = "player-list"
                            attributes["hx-get"] = "/admin/players"
                            attributes["hx-trigger"] = "every 5s load"
                     }
                }
            }
        }
    }

    get("/admin/long-term") {
        call.respondHtml {
            body {
                val history = synchronized(AdminContext.longTermHistory) { AdminContext.longTermHistory.toList() }
                if (history.isEmpty()) {
                    p("text-gray-500 italic") { +"Collecting data (samples every minute)..." }
                } else {
                    val hourly = history.takeLast(60)
                    val daily = history // Up to 1440

                    // 1. Hourly Chart (Tick Time)
                    renderHistoryChart("Last Hour - Tick Duration (ms)", hourly, 4, 100.0) { it.duration.toDouble() }
                    
                    // 2. Daily Chart (Memory)
                    renderHistoryChart("Last 24 Hours - Memory (MB)", daily, 1, 60.0, "bg-purple-600") { it.memoryUsed.toDouble() / 1024 / 1024 }
                }
            }
        }
    }

    get("/admin/ticks") {
        call.respondHtml {
            body {
                val history = synchronized(AdminContext.tickHistory) { AdminContext.tickHistory.toList() }
                if (history.isNotEmpty()) {
                     // Data Prep
                     val recent = history.takeLast(50)
                     
                     // 1. Tick Graph
                     val tickTimes = recent.map { it.duration }.sorted()
                     val maxTickTime = tickTimes.maxOrNull() ?: 100L
                     val p50 = tickTimes[(tickTimes.size * 0.5).toInt()]
                     val p95 = tickTimes[(tickTimes.size * 0.95).toInt()]
                     val p99 = tickTimes[(tickTimes.size * 0.99).toInt()]
                     
                     val tickH = 160.0
                     val tickScale = tickH / maxTickTime.toDouble().coerceAtLeast(1.0)

                     div("mb-6") {
                        h3("text-[10px] font-black text-slate-600 uppercase tracking-widest mb-2") { +"Tick Duration (ms)" }
                        div("relative h-48 border border-white/5 bg-slate-950/50 rounded-sm ml-8") {
                            // Axis
                            div("absolute left-0 -ml-12 text-[9px] font-mono text-slate-600 text-right w-10") { style = "top: 0"; +"${maxTickTime}ms" }
                            div("absolute left-0 -ml-12 text-[9px] font-mono text-slate-600 text-right w-10") { style = "bottom: 0"; +"0ms" }
                            
                            div("flex items-end h-full gap-1 p-2 overflow-hidden") {
                                recent.forEach { data ->
                                     val height = (data.duration * tickScale).coerceAtMost(tickH)
                                     div("flex flex-col-reverse w-2 relative group hover:opacity-100 opacity-90") {
                                        style = "height: ${height}px;"
                                        title = "Tick ${data.tick}: ${data.duration}ms"
                                        
                                        data.stages.forEach { (name, time) ->
                                            val sHeight = (time * tickScale).coerceAtLeast(1.0) // Min 1px visibility
                                            val color = stringToColor(name)
                                            div("w-full transition-all duration-200") {
                                                style = "height: ${sHeight}px; background-color: $color"
                                                title = "$name: ${time}ms" // Tooltip per stage
                                            }
                                        }
                                     }
                                }
                            }
                        }
                     }
                     
                     // 2. Memory Graph
                     val recentMaxMem = recent.maxOfOrNull { it.memoryUsed } ?: 1L
                     val memH = 60.0
                     val memScale = memH / (recentMaxMem.toDouble() * 1.1).coerceAtLeast(1.0) // 10% padding
                     div("mb-6") {
                         h3("text-[10px] font-black text-slate-600 uppercase tracking-widest mb-2") { +"Memory Usage" }
                         div("relative h-16 border border-white/5 bg-slate-950/50 rounded-sm ml-8") {
                             // Axis
                             div("absolute left-0 -ml-12 text-[9px] font-mono text-slate-600 text-right w-10") { style = "top: 0"; +"${(recentMaxMem * 1.1 / 1024 / 1024).toInt()}MB" }
                             div("absolute left-0 -ml-12 text-[9px] font-mono text-slate-600 text-right w-10") { style = "bottom: 0"; +"0MB" }

                             div("flex items-end h-full gap-1 p-1 overflow-hidden") {
                                 recent.forEach { data ->
                                     val height = (data.memoryUsed * memScale).coerceAtMost(memH)
                                     div("w-2 bg-purple-500 hover:bg-purple-400") {
                                         style = "height: ${height}px"
                                         title = "Mem: ${(data.memoryUsed / 1024 / 1024)}MB"
                                     }
                                 }
                             }
                         }
                     }
                     
                     // 3. CPU Graph
                     val maxLoad = recent.maxOfOrNull { it.cpuLoad } ?: 1.0
                     val cpuH = 60.0
                     val cpuScale = cpuH / (maxLoad * 1.1).coerceAtLeast(0.1) // 10% padding
                     div {
                         h3("text-[10px] font-black text-slate-600 uppercase tracking-widest mb-2") { +"System Load" }
                         div("relative h-16 border border-white/5 bg-slate-950/50 rounded-sm ml-8") {
                             // Axis
                             div("absolute left-0 -ml-12 text-[9px] font-mono text-slate-600 text-right w-10") { style = "top: 0"; +"${String.format("%.1f", maxLoad * 1.1)}" }
                             div("absolute left-0 -ml-12 text-[9px] font-mono text-slate-600 text-right w-10") { style = "bottom: 0"; +"0.0" }

                             div("flex items-end h-full gap-1 p-1 overflow-hidden") {
                                 recent.forEach { data ->
                                     val height = (data.cpuLoad * cpuScale).coerceAtMost(cpuH)
                                     div("w-2 bg-green-500 hover:bg-green-400") {
                                         style = "height: ${height}px"
                                         title = "Load: ${String.format("%.2f", data.cpuLoad)}"
                                     }
                                 }
                             }
                         }
                     }

                    // OOB Metrics Update
                    div {
                        attributes["hx-swap-oob"] = "innerHTML:#metrics-display"
                        div("grid grid-cols-2 gap-3 text-[11px] font-medium") {
                            div { span("text-slate-600 uppercase font-black text-[9px] tracking-widest block mb-0.5") { +"P50" }; span("text-white") { +"${p50}ms" } }
                            div { span("text-slate-600 uppercase font-black text-[9px] tracking-widest block mb-0.5") { +"P95" }; span("text-white") { +"${p95}ms" } }
                            div { span("text-slate-600 uppercase font-black text-[9px] tracking-widest block mb-0.5") { +"P99" }; span("text-white") { +"${p99}ms" } }
                            div { span("text-slate-600 uppercase font-black text-[9px] tracking-widest block mb-0.5") { +"Max" }; span("text-white") { +"${maxTickTime}ms" } }
                            div("col-span-2 border-t border-white/5 pt-3 mt-1") {
                                val curMem = recent.last().memoryUsed / 1024 / 1024
                                val totMem = recentMaxMem / 1024 / 1024
                                span("text-slate-600 uppercase font-black text-[9px] tracking-widest block mb-0.5") { +"Memory" }
                                span("text-white") { +"${curMem}MB / ${totMem}MB" }
                            }
                            div("col-span-2") {
                                span("text-gray-400") { +"CPU Load: " }
                                +"${String.format("%.2f", recent.last().cpuLoad)}"
                            }
                        }
                    }
                }
            }
        }
    }
    
    post("/admin/console") {
        val params = call.receiveParameters()
        val cmd = params["command"] ?: ""
        if (cmd.isNotBlank()) {
            val session = call.sessions.get<world.gregs.voidps.website.UserSession>()
            val rights = session?.rights ?: "admin" // Fallback to admin for local dev convenience if not logged in? Or None? User asked to use rights. Let's start with empty/guest if null, but for this specific "Admin Panel" context, existing was 'adminPlayer'.
            // I will use session rights if present, else keep 'admin' default as it's a dev tool running locally?
            // "When logging in it should give you the same rights".
            // Let's fallback to "admin" to preserve existing behavior for now if not logged in (e.g. localhost access without login flow), but if logged in use the rights.
            
            val effectiveRights = session?.rights ?: "admin"
            val username = session?.username ?: "Console"
            
            val player = Player(
                accountName = username,
                variables = mutableMapOf("rights" to effectiveRights)
            )
            
            try {
                Commands.call(player, cmd)
            } catch (e: Exception) {
               // Ignore
            }
            
             val messages: Iterable<*>? = player["messages"]
             
             call.respondHtml {
                 body {
                     if (messages != null) {
                         messages.forEach { msg ->
                             div { +msg.toString() }
                         }
                     }
                 }
             }
        }
    }
    
    get("/admin/players") {
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val filter = call.request.queryParameters["filter"] ?: ""
        
        call.respondHtml {
             body {
                val allPlayers = get<Players>()
                val filteredPlayers = if (filter.isBlank()) {
                    allPlayers.toList()
                } else {
                    allPlayers.filter { 
                        it.accountName.contains(filter, ignoreCase = true) || 
                        (it.client?.address ?: "").contains(filter, ignoreCase = true) 
                    }
                }
                
                val pageSize = 25
                val totalPlayers = filteredPlayers.size
                val totalPages = (totalPlayers + pageSize - 1) / pageSize
                val currentPage = page.coerceIn(1, totalPages.coerceAtLeast(1))
                
                val pagedPlayers = filteredPlayers
                    .drop((currentPage - 1) * pageSize)
                    .take(pageSize)

                div {
                    input(type = InputType.text, name = "filter", classes = "bg-slate-900 border border-white/5 text-white p-2 text-xs rounded-sm mb-4 focus:outline-none focus:border-blue-500/30 w-64") {
                        value = filter
                        placeholder = "Search players..."
                        attributes["hx-get"] = "/admin/players"
                        attributes["hx-trigger"] = "keyup changed delay:500ms"
                        attributes["hx-target"] = "#player-list"
                        attributes["name"] = "filter"
                    }
                }
                
                if (totalPlayers == 0) {
                    p { +"No players found." }
                } else {
                    table("w-full text-left text-[11px] font-medium") {
                        thead("text-slate-600 uppercase font-black text-[9px] tracking-widest border-b border-white/5") {
                            tr {
                                th(classes = "p-3") { +"Index" }
                                th(classes = "p-3") { +"Name" }
                                th(classes = "p-3") { +"Location" }
                                th(classes = "p-3") { +"IP Address" }
                                th(classes = "p-3") { +"Actions" }
                            }
                        }
                        tbody {
                            pagedPlayers.forEach { player ->
                                tr("border-b border-white/5 hover:bg-white/[0.01] transition-colors") {
                                    td(classes = "p-3") { +"${player.index}" }
                                    td(classes = "p-3 text-white font-black") { +player.accountName }
                                    td(classes = "p-3 text-slate-500 font-mono") { +"${player.tile}" }
                                    td(classes = "p-3 text-slate-500 font-mono") { +"${player.client?.address ?: "N/A"}" }
                                    td(classes = "p-3") { 
                                        a(href = "/admin/player/${player.accountName}", classes = "text-blue-500 hover:text-white transition-colors") { +"Inspect" }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Pagination
                    if (totalPages > 1) {
                         div("flex gap-2 mt-4 justify-center") {
                             if (currentPage > 1) {
                                 button(classes = "bg-gray-700 px-3 py-1 rounded") {
                                     attributes["hx-get"] = "/admin/players?page=${currentPage - 1}&filter=$filter"
                                     attributes["hx-target"] = "#player-list"
                                     +"Previous"
                                 }
                             }
                             span("self-center") { +"Page $currentPage of $totalPages" }
                             if (currentPage < totalPages) {
                                 button(classes = "bg-gray-700 px-3 py-1 rounded") {
                                     attributes["hx-get"] = "/admin/players?page=${currentPage + 1}&filter=$filter"
                                     attributes["hx-target"] = "#player-list"
                                     +"Next"
                                 }
                             }
                         }
                    }
                }
             }
         }
    }
}



fun HTML.adminLayout(session: UserSession?, content: DIV.() -> Unit) {
    commonLayout("Admin", session) {
        script(src = "https://unpkg.com/htmx.org@1.9.10") {}
        script {
            unsafe {
                +"""
                var paused = false;
                function togglePause() {
                    paused = !paused;
                    var btn = document.getElementById('pause-btn');
                    var graph = document.getElementById('tick-graph');
                    
                    if (paused) {
                        btn.innerText = "Resume";
                        btn.classList.add("bg-green-600");
                        btn.classList.remove("bg-gray-700");
                        graph.setAttribute("hx-trigger", "none");
                        htmx.process(graph);
                    } else {
                         btn.innerText = "Pause";
                         btn.classList.add("bg-gray-700");
                         btn.classList.remove("bg-green-600");
                         graph.setAttribute("hx-trigger", "every 600ms");
                         htmx.process(graph);
                    }
                }
                """
            }
        }
        
        div("container mx-auto p-4") {
             div("flex justify-between items-center mb-6") {
                 h1("text-3xl font-bold text-white") { +"Void Admin" }
                 a(href = "/", classes = "text-slate-400 hover:text-white transition-colors") { +"Back to Website" }
             }
             content()
        }
    }
}

fun FlowContent.renderHistoryChart(
    titleText: String,
    data: List<TickData>,
    barWidth: Int,
    height: Double,
    colorClass: String = "bg-blue-600",
    valueSelector: (TickData) -> Double
) {
    val maxVal = data.maxOfOrNull(valueSelector)?.coerceAtLeast(1.0) ?: 1.0
    val scale = height / maxVal
    
    div("mb-6") {
        h3("text-sm font-bold text-gray-400 mb-1") { +titleText }
        div("relative border-b border-l border-gray-700 bg-gray-900/30 rounded ml-8") {
            attributes["style"] = "height: ${height + 20}px"
            
            // Y-Axis
            div("absolute left-0 -ml-12 text-[10px] text-gray-500 text-right w-10") { attributes["style"] = "top: 0"; +"${maxVal.toInt()}" }
            div("absolute left-0 -ml-12 text-[10px] text-gray-500 text-right w-10") { attributes["style"] = "bottom: 20px"; +"0" }

            div("flex items-end h-full gap-[1px] px-1 pb-5 overflow-hidden") {
                data.forEach { d ->
                    val v = valueSelector(d)
                    val h = (v * scale).coerceAtMost(height).coerceAtLeast(1.0)
                    div("hover:opacity-100 opacity-80 transition-opacity $colorClass") {
                        attributes["style"] = "height: ${h}px; width: ${barWidth}px; ${if (colorClass.startsWith("bg-")) "" else "background-color: $colorClass"}"
                        attributes["title"] = "Tick ${d.tick}: ${String.format("%.1f", v)}"
                    }
                }
            }
        }
    }
}

fun stringToColor(str: String): String {
    var hash = 0
    for (char in str) {
        hash = char.code + ((hash shl 5) - hash)
    }
    val c = (hash and 0x00FFFFFF).toString(16).uppercase()
    return "#" + "00000".substring(0, 6 - c.length) + c
}

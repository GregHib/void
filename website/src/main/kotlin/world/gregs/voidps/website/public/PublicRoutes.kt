package world.gregs.voidps.website.public

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*

import io.ktor.server.plugins.*
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.website.UserSession
import world.gregs.voidps.website.commonLayout
import io.ktor.server.sessions.*

fun HTML.layout(title: String, session: UserSession?, showSidebar: Boolean = false, content: FlowContent.() -> Unit) {
    commonLayout(title, session) {
        div("flex min-h-[calc(100vh-64px)]") {
            // Sidebar
            if (showSidebar) {
                aside("w-64 glass border-r border-slate-900 flex-shrink-0 sticky top-16 h-[calc(100vh-64px)] overflow-y-auto") {
                    div("p-6") {
                        h2("text-xl font-black bg-gradient-to-br from-white to-slate-500 bg-clip-text text-transparent mb-8 uppercase tracking-widest") { +"Hiscores" }
                        
                        nav("space-y-1") {
                            a(href = "/highscores", classes = "block px-4 py-2 rounded-sm text-slate-400 hover:text-white skill-link transition-all text-xs font-bold uppercase tracking-wider mb-4") {
                                +"Overall"
                            }
                            
                            p("px-4 text-[10px] font-black text-slate-600 uppercase tracking-[0.2em] mb-2") { +"Skills" }
                            Skill.all.forEach { skill ->
                                a(href = "/highscores/${skill.name.lowercase()}", classes = "block px-4 py-1.5 rounded-sm text-slate-500 hover:text-blue-400 skill-link transition-all text-[11px] font-medium") {
                                    +skill.name
                                }
                            }
                        }
                    }
                }
            }
            
            // Main Content
            main("flex-1 p-8") {
                div("max-w-5xl mx-auto") {
                    content()
                }
            }
        }
    }
}

fun Route.publicRoutes() {
    get("/") {
        val session = call.sessions.get<UserSession>()
        call.respondHtml {
            commonLayout("Home", session) {
                div("min-h-[calc(100vh-64px)] flex items-center justify-center") {
                    div("text-center space-y-8") {
                        h1("text-8xl font-black tracking-tighter bg-gradient-to-b from-white to-slate-600 bg-clip-text text-transparent") { +"VOID" }
                        p("text-lg text-slate-500 max-w-md mx-auto font-medium") { +"The ultimate high-performance game server experience." }
                        div("flex gap-4 justify-center") {
                            a(href = "/highscores", classes = "px-8 py-3 bg-white text-slate-900 rounded-sm font-black uppercase text-xs tracking-[0.2em] hover:bg-slate-200 transition-all") { +"View Hiscores" }
                            if (session?.rights?.equals("admin", true) == true) {
                                a(href = "/admin", classes = "px-8 py-3 bg-slate-900 hover:bg-slate-800 rounded-sm font-black uppercase text-xs tracking-[0.2em] transition-all border border-white/5 text-white") { +"Admin Panel" }
                            }
                        }
                    }
                }
            }
        }
    }

    get("/highscores/{skill?}") {
        val skillName = call.parameters["skill"]
        val skill = if (skillName != null) Skill.of(skillName.replaceFirstChar { it.uppercase() }) else null
        val entries = Highscores.getEntries(skill)
        val title = skill?.name ?: "Overall"
        
        val session = call.sessions.get<UserSession>()
        call.respondHtml {
            layout(title, session, showSidebar = true) {
                h1("text-3xl font-black text-white mb-1 uppercase tracking-wider") { +title }
                p("text-sm text-slate-500 font-medium") { +"Top players ranked by ${if (skill == null) "Total Level" else skill.name}." }

                div("mt-8 overflow-hidden rounded-sm border border-slate-900 bg-slate-950/50 shadow-2xl") {
                    table("w-full text-left border-collapse") {
                        thead("bg-slate-900/80 text-slate-500 uppercase text-[10px] font-black tracking-widest border-b border-white/5") {
                            tr {
                                th(classes = "p-4 w-20") { +"Rank" }
                                th(classes = "p-4") { +"Player" }
                                th(classes = "p-4") { +if (skill == null) "Total Level" else "Level" }
                                th(classes = "p-4") { +"Experience" }
                                th(classes = "p-4 w-32") { +"Mode" }
                            }
                        }
                        tbody {
                            if (entries.isEmpty()) {
                                tr {
                                    td("p-12 text-center text-slate-500") {
                                        attributes["colspan"] = "5"
                                        +"No rankings available yet."
                                    }
                                }
                            } else {
                                entries.forEach { entry ->
                                    tr("border-b border-slate-800/50 hover:bg-blue-600/5 transition-colors group") {
                                        td("p-4 font-mono text-slate-500") { +"#${entry.rank}" }
                                        td("p-4") {
                                            a(href = "/highscores/player/${entry.name}", classes = "font-bold text-white hover:text-blue-400 transition-colors") {
                                                +entry.name
                                            }
                                        }
                                        td("p-4 text-blue-500 font-mono text-sm") { +"%,d".format(entry.level) }
                                        td("p-4 text-slate-400 text-sm") { +"%,d".format(entry.xp) }
                                        td("p-4") {
                                            span("px-2 py-0.5 rounded-sm text-[10px] uppercase font-black bg-slate-900 text-slate-500 border border-white/5") { +entry.mode }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    get("/highscores/player/{name}") {
        val name = call.parameters["name"] ?: throw BadRequestException("Player name required")
        val stats = Highscores.getPlayerStats(name) ?: throw NotFoundException("Player not found")
        
        val session = call.sessions.get<UserSession>()
        call.respondHtml {
            layout("${stats.name}'s Profile", session, showSidebar = true) {
                div("space-y-1") {
                    h1("text-3xl font-black text-white uppercase tracking-wider") { +stats.name }
                    div("flex gap-3 items-center") {
                        span("px-2 py-0.5 rounded-sm text-[10px] font-black uppercase bg-blue-500/5 text-blue-500 border border-blue-500/10") { +stats.mode }
                        span("text-slate-600 text-[10px] font-black uppercase tracking-widest") { +"Rights: ${stats.rights}" }
                    }
                }

                div("mt-10 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4") {
                    // Summary Card
                    val totalXp = stats.skills.values.sumOf { it.xp }
                    val totalLevel = stats.skills.values.sumOf { it.level }
                    
                    div("col-span-full glass p-6 rounded-sm flex justify-around items-center mb-4 border border-white/5") {
                        div("text-center") {
                            p("text-slate-600 text-[10px] uppercase font-black tracking-[0.2em]") { +"Total Level" }
                            p("text-3xl font-black text-white") { +"%,d".format(totalLevel) }
                        }
                        div("w-px h-10 bg-slate-900") {}
                        div("text-center") {
                            p("text-slate-600 text-[10px] uppercase font-black tracking-[0.2em]") { +"Total Experience" }
                            p("text-3xl font-black text-white") { +"%,d".format(totalXp) }
                        }
                    }

                    Skill.all.forEach { skill ->
                        val stat = stats.skills[skill] ?: SkillStat(1, 0)
                        div("glass p-4 rounded-sm flex items-center justify-between hover:border-white/10 transition-all group") {
                            div {
                                p("text-slate-400 font-bold group-hover:text-white text-xs uppercase tracking-widest") { +skill.name }
                                p("text-slate-600 text-[10px] font-mono") { +"%,d XP".format(stat.xp) }
                            }
                            div("text-right") {
                                p("text-xl font-black text-blue-500") { +"${stat.level}" }
                            }
                        }
                    }
                }
            }
        }
    }

    get("/grand-exchange") {
        val items = GrandExchange.getTrackedItems()
        val session = call.sessions.get<UserSession>()
        call.respondHtml {
            layout("Grand Exchange", session) {
                div("mb-10") {
                    h1("text-4xl font-black text-white mb-1 uppercase tracking-wider") { +"Grand Exchange" }
                    p("text-slate-500 text-sm font-medium") { +"Vibrant marketplace for every adventurer." }
                }

                div("grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6") {
                    if (items.isEmpty()) {
                        div("col-span-full py-20 text-center glass rounded-sm border-dashed border border-slate-900") {
                            p("text-slate-600 text-lg font-bold uppercase tracking-wider") { +"No items tracked" }
                        }
                    } else {
                        items.forEach { id ->
                            val name = GrandExchange.getItemName(id)
                            a(href = "/grand-exchange/$id", classes = "glass p-6 rounded-sm hover:bg-white/[0.02] transition-all group border border-white/5") {
                                div("flex items-center gap-4") {
                                    div("w-12 h-12 bg-slate-950 rounded-sm flex items-center justify-center flex-shrink-0 group-hover:scale-105 transition-transform") {
                                        span("text-xl font-black text-blue-500") { +name.take(1) }
                                    }
                                    div("text-left") {
                                        h3("text-sm font-black text-white group-hover:text-blue-500 transition-colors uppercase tracking-wider") { +name }
                                        p("text-slate-600 text-[10px] font-mono mt-0.5") { +id }
                                    }
                                }
                                div("mt-4 pt-4 border-t border-white/5 flex justify-between items-center") {
                                    span("text-[9px] font-black text-slate-600 uppercase tracking-widest") { +"Market Data" }
                                    div("text-blue-500 group-hover:translate-x-1 transition-transform text-xs") {
                                        unsafe { raw("&rarr;") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    get("/grand-exchange/{item}") {
        val itemId = call.parameters["item"] ?: throw BadRequestException("Item ID required")
        val history = GrandExchange.getHistory(itemId) ?: throw NotFoundException("Item history not found")
        val itemName = GrandExchange.getItemName(itemId)
        val timeframe = call.request.queryParameters["timeframe"] ?: "day"

        val data = when (timeframe.lowercase()) {
            "week" -> history.week
            "month" -> history.month
            "year" -> history.year
            else -> history.day
        }
        
        val session = call.sessions.get<UserSession>()
        call.respondHtml {
            layout("$itemName - Grand Exchange", session) {
                div("flex flex-col lg:flex-row gap-8 items-start lg:items-center mb-12") {
                    div("w-20 h-20 bg-slate-950 rounded-sm flex items-center justify-center flex-shrink-0 border border-white/5 shadow-xl") {
                        span("text-4xl font-black text-blue-500") { +itemName.take(1) }
                    }
                    div("flex-1 space-y-1") {
                        div("flex items-center gap-3") {
                            h1("text-4xl font-black text-white uppercase tracking-wider") { +itemName }
                            span("bg-blue-500/5 text-blue-500 text-[9px] font-black px-1.5 py-0.5 rounded-sm uppercase tracking-widest h-fit border border-blue-500/10") { +"Tradable" }
                        }
                        div("flex gap-3 items-center") {
                            span("text-slate-600 font-mono text-sm") { +itemId }
                            span("w-1 h-1 bg-slate-900 rounded-full") {}
                            span("text-slate-500 text-[10px] font-black uppercase tracking-[0.2em]") { +"Market Tracker" }
                        }
                    }
                    
                    // Timeframe Selector
                    div("flex bg-slate-900/50 p-1 rounded-sm border border-white/5") {
                        listOf("day", "week", "month", "year").forEach { tf ->
                            val active = timeframe.lowercase() == tf
                            a(href = "?timeframe=$tf", classes = "px-4 py-1.5 rounded-sm text-[10px] font-black uppercase tracking-widest transition-all ${if (active) "bg-blue-500 text-white" else "text-slate-500 hover:text-slate-300"}") {
                                +tf
                            }
                        }
                    }
                }

                val latest = history.day.maxByOrNull { it.key }?.value
                if (latest != null) {
                    div("grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-12") {
                        div("glass p-6 rounded-sm border-l-2 border-l-emerald-500/50") {
                            p("text-slate-600 text-[10px] uppercase font-black tracking-[0.2em] mb-2") { +"Market Price" }
                            div("flex items-baseline gap-2") {
                                p("text-2xl font-black text-white") { +"%,d".format(latest.close) }
                                span("text-emerald-500 text-[10px] font-black") { +"GP" }
                            }
                        }
                        div("glass p-6 rounded-sm") {
                            p("text-slate-600 text-[10px] uppercase font-black tracking-[0.2em] mb-3") { +"Daily Volatility" }
                            div("space-y-2") {
                                div("flex justify-between items-center text-[10px] font-mono") {
                                    span("text-rose-500") { +"%,d".format(latest.low) }
                                    span("text-emerald-500") { +"%,d".format(latest.high) }
                                }
                                div("h-1 bg-slate-950 rounded-full overflow-hidden") {
                                    div("h-full bg-slate-800 rounded-full") {
                                        attributes["style"] = "width: 100%"
                                    }
                                }
                            }
                        }
                        div("glass p-6 rounded-sm") {
                            p("text-slate-600 text-[10px] uppercase font-black tracking-[0.2em] mb-2") { +"Traded Today" }
                            div("flex items-baseline gap-2") {
                                p("text-2xl font-black text-white") { +"%,d".format(latest.volume) }
                                span("text-slate-600 text-[10px] font-black") { +"UNITS" }
                            }
                        }
                        div("glass p-6 rounded-sm border-r-2 border-r-blue-500/50") {
                            p("text-slate-600 text-[10px] uppercase font-black tracking-[0.2em] mb-2") { +"Activity" }
                            div("flex items-baseline gap-2") {
                                p("text-2xl font-black text-blue-500") { +"%,d".format(latest.count) }
                                span("text-slate-600 text-[10px] font-black") { +"TRADES" }
                            }
                        }
                    }
                }

                if (latest != null) {
                    div("grid grid-cols-1 lg:grid-cols-2 gap-4 mb-12") {
                        div("glass p-6 rounded-sm border border-white/5 shadow-2xl") {
                            h3("text-[10px] font-black text-slate-500 uppercase tracking-[0.2em] mb-4") { +"Buy/Sell Price Trend" }
                            div("h-[250px]") {
                                canvas { id = "priceChart" }
                            }
                        }
                        div("glass p-6 rounded-sm border border-white/5 shadow-2xl") {
                            h3("text-[10px] font-black text-slate-500 uppercase tracking-[0.2em] mb-4") { +"Trade Volume Trend" }
                            div("h-[250px]") {
                                canvas { id = "volumeChart" }
                            }
                        }
                    }

                    val historyData = data.entries.sortedBy { it.key }
                    val labels = historyData.map { java.time.Instant.ofEpochMilli(it.key).atZone(java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("MMM dd")) }
                    val buyPrices = historyData.map { it.value.averageLow }
                    val sellPrices = historyData.map { it.value.averageHigh }
                    val buyVolumes = historyData.map { it.value.volumeLow }
                    val sellVolumes = historyData.map { it.value.volumeHigh }

                    script {
                        src = "https://cdn.jsdelivr.net/npm/chart.js"
                    }
                    script {
                        unsafe {
                            raw("""
                                document.addEventListener('DOMContentLoaded', function() {
                                    const ctxPrice = document.getElementById('priceChart').getContext('2d');
                                    new Chart(ctxPrice, {
                                        type: 'line',
                                        data: {
                                            labels: ${labels.joinToString("', '", "['", "']")},
                                            datasets: [
                                                {
                                                    label: 'Buy Price',
                                                    data: ${buyPrices.joinToString(", ", "[", "]")},
                                                    borderColor: '#10b981',
                                                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                                                    fill: true,
                                                    tension: 0.4,
                                                    pointRadius: 2
                                                },
                                                {
                                                    label: 'Sell Price',
                                                    data: ${sellPrices.joinToString(", ", "[", "]")},
                                                    borderColor: '#3b82f6',
                                                    backgroundColor: 'rgba(59, 130, 246, 0.1)',
                                                    fill: true,
                                                    tension: 0.4,
                                                    pointRadius: 2
                                                }
                                            ]
                                        },
                                        options: {
                                            responsive: true,
                                            maintainAspectRatio: false,
                                            plugins: {
                                                legend: { display: false }
                                            },
                                            scales: {
                                                x: { display: false },
                                                y: { 
                                                    grid: { color: 'rgba(255, 255, 255, 0.05)' },
                                                    ticks: { color: '#64748b', font: { size: 10 } }
                                                }
                                            }
                                        }
                                    });

                                    const ctxVolume = document.getElementById('volumeChart').getContext('2d');
                                    new Chart(ctxVolume, {
                                        type: 'bar',
                                        data: {
                                            labels: ${labels.joinToString("', '", "['", "']")},
                                            datasets: [
                                                {
                                                    label: 'Buy Volume',
                                                    data: ${buyVolumes.joinToString(", ", "[", "]")},
                                                    backgroundColor: 'rgba(16, 185, 129, 0.5)'
                                                },
                                                {
                                                    label: 'Sell Volume',
                                                    data: ${sellVolumes.joinToString(", ", "[", "]")},
                                                    backgroundColor: 'rgba(59, 130, 246, 0.5)'
                                                }
                                            ]
                                        },
                                        options: {
                                            responsive: true,
                                            maintainAspectRatio: false,
                                            plugins: {
                                                legend: { display: false }
                                            },
                                            scales: {
                                                x: { display: false },
                                                y: { 
                                                    stacked: true,
                                                    grid: { color: 'rgba(255, 255, 255, 0.05)' },
                                                    ticks: { color: '#64748b', font: { size: 10 } }
                                                }
                                            }
                                        }
                                    });
                                });
                            """.trimIndent())
                        }
                    }
                }

                div("glass rounded-sm overflow-hidden shadow-2xl border border-white/5") {
                    div("px-6 py-4 bg-slate-950/50 border-b border-white/5 flex justify-between items-center") {
                        h2("text-lg font-black text-white uppercase tracking-widest") { +"Market History" }
                        div("flex gap-2") {
                            span("px-2 py-0.5 bg-blue-500/5 text-blue-500 rounded-sm text-[9px] font-black uppercase tracking-widest border border-blue-500/10") { +timeframe.replaceFirstChar { it.uppercase() } }
                        }
                    }
                    div("overflow-x-auto") {
                        table("w-full text-left border-collapse") {
                            thead("bg-slate-950/80 text-slate-600 uppercase text-[9px] font-black tracking-[0.2em]") {
                                tr {
                                    th(classes = "p-4") { +"Date" }
                                    th(classes = "p-4") { +"Open" }
                                    th(classes = "p-4") { +"Close" }
                                    th(classes = "p-4 text-center") { +"Trend" }
                                    th(classes = "p-4") { +"High" }
                                    th(classes = "p-4") { +"Low" }
                                    th(classes = "p-4") { +"Volume" }
                                }
                            }
                            tbody {
                                if (data.isEmpty()) {
                                    tr {
                                        td("p-20 text-center text-slate-600 italic") {
                                            attributes["colspan"] = "7"
                                            +"No data available for this timeframe."
                                        }
                                    }
                                } else {
                                    data.entries.sortedByDescending { it.key }.forEach { (timestamp, aggregate) ->
                                        val date = java.time.Instant.ofEpochMilli(timestamp)
                                            .atZone(java.time.ZoneId.systemDefault())
                                            .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                        val isUp = aggregate.close >= aggregate.open
                                        val change = if (aggregate.open != 0) ((aggregate.close - aggregate.open).toDouble() / aggregate.open * 100) else 0.0
                                        
                                        tr("border-b border-white/5 hover:bg-white/[0.01] transition-colors font-medium") {
                                            td("p-4 text-slate-400 text-xs") { +date }
                                            td("p-4 text-slate-500 font-mono text-xs") { +"%,d".format(aggregate.open) }
                                            td("p-4 text-white font-black text-sm") { +"%,d".format(aggregate.close) }
                                            td("p-4 text-center") {
                                                div("flex flex-col items-center gap-0.5") {
                                                    span("text-[10px] ${if (isUp) "text-emerald-500" else "text-rose-500"}") {
                                                        +(if (isUp) "▲" else "▼")
                                                    }
                                                    span("text-[9px] font-black ${if (isUp) "text-emerald-600" else "text-rose-600"}") {
                                                        +"%.2f%%".format(change)
                                                    }
                                                }
                                            }
                                            td("p-4 text-emerald-500/70 text-xs") { +"%,d".format(aggregate.high) }
                                            td("p-4 text-rose-500/70 text-xs") { +"%,d".format(aggregate.low) }
                                            td("p-4 text-slate-600 text-xs font-bold") { +"%,d".format(aggregate.volume) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

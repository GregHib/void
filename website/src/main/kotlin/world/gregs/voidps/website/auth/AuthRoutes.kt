package world.gregs.voidps.website.auth

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.website.admin.adminLayout
import world.gregs.voidps.website.commonLayout
import world.gregs.voidps.website.UserSession

fun Route.authRoutes() {

    get("/login") {
        call.respondLogin()
    }

    post("/login") {
        val params = call.receiveParameters()
        val username = params["username"] ?: ""
        val password = params["password"] ?: ""

        val account = get<AccountDefinitions>().getByAccount(username)
        if (account != null && BCrypt.checkpw(password, account.passwordHash)) {
            val player = get<world.gregs.voidps.engine.data.Storage>().load(username)
            val rights = player?.variables?.get("rights") as? String ?: "none"
            call.sessions.set(UserSession(username, rights))
            call.respondRedirect("/admin")
        } else {
            call.respondLogin(error = "Invalid credentials", username = username)
        }
    }

    get("/logout") {
        call.sessions.clear<UserSession>()
        call.respondRedirect("/")
    }
}

private suspend fun ApplicationCall.respondLogin(error: String? = null, username: String = "") {
    val session = sessions.get<UserSession>()
    respondHtml {
        commonLayout("Login", session) {
            div("flex items-center justify-center min-h-[calc(100vh-64px)]") {
                div("bg-gray-800 p-8 rounded shadow-lg w-96 border border-white/10") {
                    h1("text-2xl font-bold mb-6 text-center text-white") { +"Login to Void" }

                    if (error != null) {
                        div("mb-4 p-3 bg-red-900/30 border border-red-500/50 rounded text-red-100 text-sm text-center") {
                            +error
                        }
                    }

                    form(method = FormMethod.post, action = "/login") {
                        id = "login-form"
                        div("mb-4") {
                            label("block text-gray-400 text-sm font-bold mb-2") { +"Username" }
                            input(type = InputType.text, name = "username", classes = "w-full bg-gray-700 text-white p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500") {
                                id = "username"
                                value = username
                                onKeyDown = "if(event.key === 'Enter') { event.preventDefault(); document.getElementById('password').focus(); }"
                            }
                        }
                        div("mb-6") {
                            label("block text-gray-400 text-sm font-bold mb-2") { +"Password" }
                            input(type = InputType.password, name = "password", classes = "w-full bg-gray-700 text-white p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500") {
                                id = "password"
                                onKeyDown = "if(event.key === 'Enter') { event.preventDefault(); document.getElementById('login-form').submit(); }"
                            }
                        }
                        div("flex items-center justify-between") {
                            button(classes = "bg-blue-600 hover:bg-blue-500 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline w-full transition-colors") {
                                +"Sign In"
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Route.playerRoutes() {

    get("/admin/player/{name}") {
        val name = call.parameters["name"] ?: return@get call.respondText("Missing name")
        val player = get<Players>().get(name)
        val session = call.sessions.get<UserSession>()
        
        if (player == null) {
             call.respondHtml {
                 adminLayout(session) {
                     div("bg-red-900/50 text-white p-4 rounded border border-red-500/50") {
                         h1("text-2xl font-bold") { +"Player Offline" }
                         p { +"The player $name is currently not logged in." }
                         a(href = "/admin/players", classes = "text-blue-300 hover:underline") { +"Back to Player List" }
                     }
                 }
             }
            return@get
        }

        call.respondHtml {
            adminLayout(session) {
                div("bg-gray-800 p-4 rounded") {
                    div("flex justify-between items-start mb-4") {
                        h2("text-2xl font-bold") { +player.accountName }
                        div {
                             span("px-2 py-1 rounded bg-blue-600 text-sm mr-2") { +"Index: ${player.index}" }
                             if (player["logout", false]) span("px-2 py-1 rounded bg-red-600 text-sm") { +"Logging out..." }
                        }
                    }
                    
                    div("grid grid-cols-1 md:grid-cols-2 gap-4") {
                        // Details
                        div {
                            h3("text-xl font-bold mb-2") { +"Details" }
                             div {
                                 attributes["hx-get"] = "/admin/player/$name/details"
                                 attributes["hx-trigger"] = "every 1s"
                                 +"Loading details..."
                             }
                        }
                        
                        // Variables
                        div {
                            h3("text-xl font-bold mb-2") { +"Variables" }
                            input(type = InputType.text, classes = "w-full bg-gray-700 text-white p-2 rounded mb-2") {
                                placeholder = "Filter variables..."
                                attributes["name"] = "filter"
                                attributes["hx-get"] = "/admin/player/$name/variables"
                                attributes["hx-trigger"] = "keyup changed delay:500ms"
                                attributes["hx-target"] = "#variable-list"
                            }
                            div("h-64 overflow-y-auto bg-gray-900 p-2 rounded font-mono text-sm") {
                                id = "variable-list"
                                attributes["hx-get"] = "/admin/player/$name/variables"
                                attributes["hx-trigger"] = "load"
                            }
                        }
                    }
                    
                    div("mt-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4") {
                         // Skills
                         div {
                             h3("text-lg font-bold mb-2") { +"Skills" }
                             div {
                                 attributes["hx-get"] = "/admin/player/$name/skills"
                                 attributes["hx-trigger"] = "every 5s"
                             }
                         }
                         // Inventory
                         div {
                             h3("text-lg font-bold mb-2") { +"Inventory" }
                             div {
                                 attributes["hx-get"] = "/admin/player/$name/inventory/inventory"
                                 attributes["hx-trigger"] = "every 1s"
                             }
                         }
                         // Equipment
                         div {
                             h3("text-lg font-bold mb-2") { +"Equipment" }
                             div {
                                 attributes["hx-get"] = "/admin/player/$name/inventory/worn_equipment"
                                 attributes["hx-trigger"] = "every 1s"
                             }
                         }
                         // Bank
                         div {
                             h3("text-lg font-bold mb-2") { +"Bank" }
                             div {
                                 attributes["hx-get"] = "/admin/player/$name/inventory/bank"
                                 attributes["hx-trigger"] = "every 1s"
                             }
                         }
                    }
                }
            }
        }
    }

    get("/admin/player/{name}/skills") {
        val name = call.parameters["name"] ?: return@get
        val player = get<Players>().get(name) ?: return@get
        
        call.respondHtml {
            body {
                div("grid grid-cols-2 gap-1 text-xs") {
                    Skill.all.forEach { skill ->
                         val level = player.levels.getMax(skill)
                         val xp = player.experience.get(skill)
                         div("bg-black p-1 rounded flex justify-between items-center px-2") {
                             span("font-bold text-gray-400 w-20") { +skill.name }
                             span("text-white") { +"$level" }
                             span("text-gray-600") { +"${xp.toInt()} xp" }
                         }
                    }
                }
            }
        }
    }

    get("/admin/player/{name}/details") {
        val name = call.parameters["name"] ?: return@get
        val player = get<Players>().get(name) ?: return@get
        call.respondHtml {
            body {
                p { +"Tile: ${player.tile}" }
                p { +"Mode: ${player.mode::class.simpleName}" }
                p { +"Rights: ${player.rights}" }
                p { +"Client: ${player.client?.address ?: "N/A"}" }
            }
        }
    }

    get("/admin/player/{name}/variables") {
        val name = call.parameters["name"] ?: return@get
        val player = get<Players>().get(name) ?: return@get
        val filter = call.request.queryParameters["filter"] ?: ""
        
        call.respondHtml {
            body {
                val vars = player.variables.data
                table("w-full") {
                    vars.filter { it.key.contains(filter, true) }.forEach { (k, v) ->
                        tr {
                            td("text-gray-400 pr-2") { +k }
                            td("text-green-400") { +v.toString() }
                        }
                    }
                }
            }
        }
    }

    get("/admin/player/{name}/inventory/{type}") {
        val name = call.parameters["name"] ?: return@get
        val type = call.parameters["type"] ?: "inventory"
        val player = get<Players>().get(name) ?: return@get
        
        // Access inventory safely
        val inventory = player.inventories.inventory(type)
        
        call.respondHtml {
             body {
                 div("grid grid-cols-4 gap-1") {
                     inventory.items.forEach { item ->
                         if (item.id.isNotEmpty()) {
                             div("bg-black p-1 text-center text-xs relative group") {
                                 title = item.id
                                 span("block") { +item.id } // Just ID for now as requested
                                 if (item.amount > 1) {
                                     span("absolute top-0 right-0 text-yellow-400 font-bold") { +"${item.amount}" }
                                 }
                             }
                         } else {
                             div("bg-gray-700 h-8 opacity-25") {}
                         }
                     }
                 }
             }
        }
    }
}

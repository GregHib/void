package content.entity.player.command

import content.bot.BotManager
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.AccountDefinitionsReloader
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.loadNpcSpawns
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.loadItemSpawns
import world.gregs.voidps.engine.entity.obj.loadObjectSpawns
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.encode.systemUpdate
import java.util.concurrent.TimeUnit
import kotlin.text.isBlank
import kotlin.text.split
import kotlin.text.toIntOrNull

class ServerCommands(val accountLoader: PlayerAccountLoader, val accountReloader: AccountDefinitionsReloader) : Script {

    init {
        adminCommand(
            "update",
            stringArg("time", desc = "Time unit (e.g. 100=1 minute or 1h 2m)", optional = true),
            desc = "Start a system shutdown after a set amount of time",
            handler = ::update,
        )
        val configs = setOf(
            "books", "teleports", "music_tracks", "fairy_rings", "ships", "objects", "items", "bots", "npcs", "areas", "emotes", "anims", "containers", "graphics",
            "item_on_item", "sounds", "quests", "midis", "variables", "music", "interfaces", "spells", "patrols", "prayers", "drops", "client_scripts", "settings", "accounts",
            "enums", "structs", "hunt_modes", "weapon_styles", "weapon_animations", "ammo", "canoes", "item_spawns", "nav",
        )
        adminCommand(
            "reload",
            stringArg("config-type", "type of content config file to reload", autofill = configs),
            desc = "Reload configuration files for the game server",
            handler = ::reload,
        )
    }

    fun reload(player: Player, args: List<String>) {
        val files = configFiles()
        val alias = args.joinToString("_")
        val loader = loaders[alias]
        if (loader != null) {
            if (loader == "items") {
                FloorItems.clear()
            }
            val reloaded = Main.reload(loader, files)
            if ("books" in reloaded) {
                val menu = player.menu
                if (menu != null && menu.startsWith("book_")) {
                    player.open(menu)
                }
            }
            if ("objects" in reloaded) {
                loadObjectSpawns(files.list(Settings["spawns.objects"]))
            }
            if ("items" in reloaded) {
                loadItemSpawns(files.list(Settings["spawns.items"]))
            }
            if ("npc_spawns" in reloaded) {
                loadNpcSpawns()
            }
            return
        }
        when (alias) {
            "item_spawns", "floor_item_spawns" -> {
                FloorItems.clear()
                loadItemSpawns(files.list(Settings["spawns.items"]))
            }
            "settings", "setting", "game_setting", "game_settings", "games_settings", "properties", "props" -> {
                Settings.load()
                SettingsReload.now()
            }
            "accounts", "account", "passwords" -> {
                if (!accountReloader.reload { count -> player.message("Reloaded $count account definitions.", ChatType.Console) }) {
                    player.message("Account reload already in progress.", ChatType.Console)
                }
            }
            "bots" -> get<BotManager>().load(files)
        }
    }

    fun update(player: Player, args: List<String>) {
        var ticks = 0
        val content = args.joinToString(" ")
        val input = args[0].toIntOrNull()
        if (input == null) {
            if (content.isBlank()) {
                shutdown(player, 0)
                return
            }
            for (part in content.split(" ")) {
                when {
                    part.endsWith("h") -> ticks += TimeUnit.HOURS.toTicks(part.removeSuffix("h").toInt())
                    part.endsWith("m") -> ticks += TimeUnit.MINUTES.toTicks(part.removeSuffix("m").toInt())
                    part.endsWith("s") -> ticks += TimeUnit.SECONDS.toTicks(part.removeSuffix("s").toInt())
                }
            }
            if (ticks == 0) {
                player.message("Unknown input '$content' please use hours minutes or seconds - e.g. 4h 20m 5s.", ChatType.Console)
                return
            }
        } else {
            ticks = input
        }
        if (ticks >= Short.MAX_VALUE) {
            player.message("Update cannot exceed ${Short.MAX_VALUE} ticks (5 hours 26 mins 43 seconds)", ChatType.Console)
            return
        }
        if (ticks < 1) {
            player.message("Update time must be positive.", ChatType.Console)
            return
        }
        for (p in Players) {
            p.client?.systemUpdate(ticks)
        }
        shutdown(player, (ticks - 2).coerceAtLeast(0))
    }

    fun shutdown(player: Player, ticks: Int) {
        AuditLog.event(player, "started_shutdown", ticks)
        // Prevent players logging-in 1 minute before update
        World.queue("system_shutdown", (ticks - 100).coerceAtLeast(0)) {
            accountLoader.update = true
        }
        World.queue("system_update", ticks) {
            Main.server.stop()
        }
    }

    private companion object {
        /** Reload command arguments to the name of the loader they reload */
        val loaders = buildMap {
            fun alias(loader: String, vararg names: String) {
                for (name in names) {
                    put(name, loader)
                }
            }
            alias("combat", "combat")
            alias("books", "book", "books")
            alias("object_teleports", "stairs", "tele", "teles", "teleports")
            alias("music_tracks", "tracks", "songs", "music_tracks")
            alias("objects", "objects", "objs")
            alias("items", "item_defs", "items", "floor_items")
            alias("enums", "enum", "enums")
            alias("structs", "struct", "structs")
            alias("hunt_modes", "hunt", "hunt_modes", "hunt_mode")
            alias("weapon_styles", "weapon_styles", "weapon_style", "styles")
            alias("weapon_animations", "weapon_animations", "weapon_anims", "weapon_animation")
            alias("ammo", "ammo", "ammo_groups", "ammunition")
            alias("canoes", "canoe", "canoes")
            alias("npcs", "npcs", "npc_defs")
            alias("npc_spawns", "npc_spawns", "spawns")
            alias("areas", "areas")
            alias("render_emotes", "emotes", "render_anims", "render_emotes")
            alias("animations", "anim_defs", "anims", "animations")
            alias("inventories", "container_defs", "containers", "inventory_defs", "inventories", "inv_defs", "invs", "shop", "shops")
            alias("graphics", "graphic_defs", "graphics", "gfx", "gfxs")
            alias("item_on_item", "item_on_item", "item-on-item", "ioi", "recipes")
            alias("sounds", "sound", "sounds", "sound effects")
            alias("farming", "produce", "farming")
            alias("quests", "quest", "quests")
            alias("midis", "midi", "midis")
            alias("variables", "vars", "variables")
            alias("jingles", "music", "music effects", "jingles")
            alias("interfaces", "interfaces")
            alias("patrols", "patrols", "paths")
            alias("prayers", "prayers")
            alias("drop_tables", "drops", "drop_tables")
            alias("client_scripts", "cs2", "cs2s", "client_scripts")
            alias("tables", "tables", "rows", "dbs", "spells")
            alias("nav_edges", "nav", "nav_graph", "nav_edges", "navigation", "ai_graph")
        }
    }
}

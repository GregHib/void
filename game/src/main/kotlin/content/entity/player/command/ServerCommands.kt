package content.entity.player.command

import content.bot.interact.navigation.graph.NavigationGraph
import content.entity.obj.ObjectTeleports
import content.entity.obj.ship.CharterShips
import content.entity.player.modal.book.Books
import content.entity.world.music.MusicTracks
import content.quest.member.fairy_tale_part_2.fairy_ring.FairyRingCodes
import content.skill.farming.FarmingDefinitions
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.ClientScriptDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ItemOnItemDefinitions
import world.gregs.voidps.engine.data.definition.JingleDefinitions
import world.gregs.voidps.engine.data.definition.MidiDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.data.definition.RenderEmoteDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.data.find
import world.gregs.voidps.engine.data.list
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.loadNpcSpawns
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.ItemSpawns
import world.gregs.voidps.engine.entity.item.floor.loadItemSpawns
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.loadObjectSpawns
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.encode.systemUpdate
import java.util.concurrent.TimeUnit
import kotlin.getValue
import kotlin.text.isBlank
import kotlin.text.split
import kotlin.text.toIntOrNull

class ServerCommands : Script {

    val players: Players by inject()
    val accountLoader: PlayerAccountLoader by inject()

    init {
        adminCommand(
            "update",
            stringArg("time", desc = "Time unit (e.g. 100=1 minute or 1h 2m)", optional = true),
            desc = "Start a system shutdown after a set amount of time",
            handler = ::update,
        )
        val configs = setOf(
            "books", "teleports", "music_tracks", "fairy_rings", "ships", "objects", "items", "nav_graph", "npcs", "areas", "emotes", "anims", "containers", "graphics",
            "item_on_item", "sounds", "quests", "midis", "variables", "music", "interfaces", "spells", "patrols", "prayers", "drops", "client_scripts", "settings",
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
        when (args.joinToString("_")) {
            "book", "books" -> get<Books>().load(files.list(Settings["definitions.books"]))
            "stairs", "tele", "teles", "teleports" -> get<ObjectTeleports>().load(files.list(Settings["map.teleports"]))
            "tracks", "songs", "music_tracks" -> get<MusicTracks>().load(files.find(Settings["map.music"]))
            "fairy_ring", "fairy_rings", "fairy_codes" -> get<FairyRingCodes>().load(files.find(Settings["definitions.fairyCodes"]))
            "ships" -> get<CharterShips>().load(files.find(Settings["map.ships.prices"]))
            "objects", "objs" -> {
                val defs: ObjectDefinitions = get()
                val custom: GameObjects = get()
                defs.load(files.list(Settings["definitions.objects"]))
                loadObjectSpawns(custom, files.list(Settings["spawns.objects"]), defs)
            }
            "item_defs", "items", "floor_items" -> {
                val items: FloorItems = get()
                val itemSpawns: ItemSpawns = get()
                items.clear()
                val itemDefinitions: ItemDefinitions = get()
                itemDefinitions.load(files.list(Settings["definitions.items"]))
                loadItemSpawns(items, itemSpawns, files.list(Settings["spawns.items"]), itemDefinitions)
            }
            "nav_graph", "ai_graph" -> get<NavigationGraph>().load(files.find(Settings["map.navGraph"]))
            "npcs" -> {
                val npcDefs = get<NPCDefinitions>()
                npcDefs.load(files.list(Settings["definitions.npcs"]))
                val npcs: NPCs = get()
                loadNpcSpawns(npcs, files.list(Settings["spawns.npcs"]), npcDefs)
            }
            "areas" -> get<AreaDefinitions>().load(files.list(Settings["map.areas"]))
            "emotes", "render_anims", "render_emotes" -> get<RenderEmoteDefinitions>().load(files.find(Settings["definitions.renderEmotes"]))
            "anim_defs", "anims", "animations" -> get<AnimationDefinitions>().load(files.list(Settings["definitions.animations"]))
            "container_defs", "containers", "inventory_defs", "inventories", "inv_defs", "invs", "shop", "shops" -> {
                get<InventoryDefinitions>().load(files.list(Settings["definitions.inventories"]), files.list(Settings["definitions.shops"]))
            }
            "graphic_defs", "graphics", "gfx", "gfxs" -> get<GraphicDefinitions>().load(files.list(Settings["definitions.graphics"]))
            "item_on_item", "item-on-item", "ioi" -> get<ItemOnItemDefinitions>().load(files.list(Settings["definitions.itemOnItem"]))
            "sound", "sounds", "sound effects" -> get<SoundDefinitions>().load(files.list(Settings["definitions.sounds"]))
            "produce", "farming" -> get<FarmingDefinitions>().load(files.find(Settings["definitions.produce"]))
            "quest", "quests" -> get<QuestDefinitions>().load(files.find(Settings["definitions.quests"]))
            "midi", "midis" -> get<MidiDefinitions>().load(files.list(Settings["definitions.midis"]))
            "vars", "variables" -> get<VariableDefinitions>().load(
                files.list(Settings["definitions.variables.players"]),
                files.list(Settings["definitions.variables.bits"]),
                files.list(Settings["definitions.variables.clients"]),
                files.list(Settings["definitions.variables.strings"]),
                files.list(Settings["definitions.variables.customs"]),
            )
            "music", "music effects", "jingles" -> get<JingleDefinitions>().load(files.list(Settings["definitions.jingles"]))
            "interfaces" -> get<InterfaceDefinitions>().load(files.list(Settings["definitions.interfaces"]), files.find(Settings["definitions.interfaces.types"]))
            "spells" -> get<SpellDefinitions>().load(files.find(Settings["definitions.spells"]))
            "patrols", "paths" -> get<PatrolDefinitions>().load(files.list(Settings["definitions.patrols"]))
            "prayers" -> get<PrayerDefinitions>().load(files.find(Settings["definitions.prayers"]))
            "drops", "drop_tables" -> get<DropTables>().load(files.list(Settings["spawns.drops"]))
            "cs2", "cs2s", "client_scripts" -> get<ClientScriptDefinitions>().load(files.list(Settings["definitions.clientScripts"]))
            "settings", "setting", "game_setting", "game_settings", "games_settings", "properties", "props" -> {
                Settings.load()
                SettingsReload.now()
            }
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
        for (p in players) {
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
}

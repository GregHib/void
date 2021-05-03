import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.data.StorageStrategy
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPCSpawns
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.effect.Hidden
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.tele
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.item.FloorItemSpawns
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.entity.obj.Stairs
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionReader
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop
import world.gregs.voidps.world.interact.entity.player.music.MusicTracks
import java.util.concurrent.atomic.AtomicInteger

on<Command>({ prefix == "tele" || prefix == "tp" }) { player: Player ->
    if (content.contains(",")) {
        val params = content.split(",")
        val plane = params[0].toInt()
        val x = params[1].toInt() shl 6 or params[3].toInt()
        val y = params[2].toInt() shl 6 or params[4].toInt()
        player.tele(x, y, plane)
    } else {
        val parts = content.split(" ")
        if (parts.size == 1) {
            player.tele(Region(parts[0].toInt()).tile.add(32, 32))
        } else {
            player.tele(parts[0].toInt(), parts[1].toInt(), if (parts.size > 2) parts[2].toInt() else 0)
        }
    }
}

val players: Players by inject()

on<Command>({ prefix == "teletome" }) { player: Player ->
    val other = players.indexed.firstOrNull { it?.name.equals(content, true) } ?: return@on
    other.tele(player.tile)
}

on<Command>({ prefix == "teleto" }) { player: Player ->
    val other = players.indexed.firstOrNull { it?.name.equals(content, true) } ?: return@on
    player.tele(other.tile)
}

on<Command>({ prefix == "npc" }) { player: Player ->
    val id = content.toIntOrNull()
    val defs: NPCDefinitions = get()
    val npcs: NPCs = get()
    val npc = if (id != null) {
        npcs.add(defs.getName(id), player.tile, Direction.NORTH)
    } else {
        println("""
                - name: $content
                  x: ${player.tile.x}
                  y: ${player.tile.y}
                  plane: ${player.tile.plane}
            """.trimIndent())
        npcs.add(content, player.tile, Direction.NORTH)
    }
//    npc?.movement?.frozen = true
}

val botCounter = AtomicInteger(0)

val loginQueue: LoginQueue by inject()
val playerStorage: StorageStrategy<Player> by inject()

on<Command>({ prefix == "save" }) { player: Player ->
    playerStorage.save(player.name, player)
}

val definitions: ItemDefinitions by inject()

on<Command>({ prefix == "item" }) { player: Player ->
    val parts = content.split(" ")
    val id = definitions.getNameOrNull(parts[0].toIntOrNull() ?: -1) ?: parts[0].toLowerCase()
    var amount = parts.getOrNull(1) ?: "1"
    if (amount == "max") {
        amount = Int.MAX_VALUE.toString()
    }
    player.inventory.add(id, amount.toInt())
    println(player.inventory.result)
}

on<Command>({ prefix == "find" }) { player: Player ->
    val items: ItemDefinitions = get()
    val search = content.toLowerCase()
    var found = false
    repeat(items.size) { id ->
        val def = items.getOrNull(id) ?: return@repeat
        if (def.name.toLowerCase().contains(search)) {
            player.message("[${def.name.toLowerCase()}] - id: $id", ChatType.Console)
            found = true
        }
    }
    if (!found) {
        player.message("No results found for '$search'", ChatType.Console)
    }
}

on<Command>({ prefix == "clear" }) { player: Player ->
    player.inventory.clearAll()
}

on<Command>({ prefix == "master" }) { player: Player ->
    player.setVar("life_points", 990)
    for (skill in Skill.all) {
        player.experience.set(skill, 14000000.0)
    }
}

on<Command>({ prefix == "hide" }) { player: Player ->
    player.effects.toggle(Hidden)
}

on<Command>({ prefix == "pos" || prefix == "mypos" }) { player: Player ->
    player.message(player.tile.toString())
    println(player.tile)
}

on<Command>({ prefix == "reload" }) { player: Player ->
    var reloadRegions = false
    when (content) {
        "stairs" -> get<Stairs>().load()
        "music", "tracks", "songs" -> get<MusicTracks>().load()
        "floor items" -> {
            val items: FloorItems = get()
            items.chunks.forEach { (_, set) ->
                set.forEach {
                    items.remove(it)
                }
            }
            val spawns: FloorItemSpawns = get()
            spawns.load()
            reloadRegions = true
        }
        "npcs" -> {
            val npcs: NPCs = get()
            npcs.forEach {
                npcs.remove(it)
            }
            val spawns: NPCSpawns = get()
            spawns.load()
            reloadRegions = true
        }
        "objects" -> {
            val objects: CustomObjects = get()
            objects.spawns.forEach { (_, set) ->
                set.forEach {
                    objects.remove(it)
                }
            }
            objects.load()
            reloadRegions = true
        }
        "interfaces" -> {}
        "nav graph", "ai graph" -> get<NavigationGraph>().load()
        "areas" -> get<Areas>().load()
    }
    if (reloadRegions) {
        val regions: RegionReader = get()
        regions.clear()
        val players: Players = get()
        players.forEach {
            regions.load(it.tile.region)
        }
    }
}

on<Command>({ prefix == "shop" }) { player: Player ->
    player.events.emit(OpenShop(content))
}
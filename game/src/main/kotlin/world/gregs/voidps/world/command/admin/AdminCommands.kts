import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.data.StorageStrategy
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.entity.character.player.skill.Levels
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.tele
import world.gregs.voidps.engine.entity.definition.*
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionReader
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.encode.playJingle
import world.gregs.voidps.network.encode.playMIDI
import world.gregs.voidps.network.encode.playSoundEffect
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.utility.func.toSILong
import world.gregs.voidps.utility.func.toUnderscoreCase
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.isCurses
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop
import world.gregs.voidps.world.interact.entity.player.effect.skull
import world.gregs.voidps.world.interact.entity.player.energy.MAX_ENERGY
import world.gregs.voidps.world.interact.entity.player.music.MusicTracks
import world.gregs.voidps.world.interact.entity.player.music.playTrack
import world.gregs.voidps.world.interact.entity.sound.playJingle
import world.gregs.voidps.world.interact.entity.sound.playMidi
import world.gregs.voidps.world.interact.entity.sound.playSound
import world.gregs.voidps.world.interact.world.Stairs

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
    npc?.events?.emit(Registered)
//    npc?.movement?.frozen = true
}

val playerStorage: StorageStrategy<Player> by inject()

on<Command>({ prefix == "save" }) { _: Player ->
    players.forEach {
        playerStorage.save(it.name, it)
    }
}

val definitions: ItemDefinitions by inject()

on<Command>({ prefix == "item" }) { player: Player ->
    val parts = content.split(" ")
    val id = definitions.getNameOrNull(parts[0].toIntOrNull() ?: -1) ?: parts[0].toLowerCase()
    val amount = parts.getOrNull(1) ?: "1"
    player.inventory.add(id, if (amount == "max") {
        Int.MAX_VALUE
    } else {
        amount.toSILong().toInt()
    }, coerce = true)
    println(player.inventory.result)
}

on<Command>({ prefix == "give" }) { player: Player ->
    val parts = content.split(" ")
    val id = definitions.getNameOrNull(parts.first().toIntOrNull() ?: -1) ?: parts.first().toLowerCase()
    val amount = parts[1]
    val name = content.removePrefix("${parts[0]} ${parts[1]} ")
    val target = players.indexed.filterNotNull().firstOrNull { it.name == name }
    if (target == null) {
        player.message("Couldn't find player $target")
    } else {
        target.inventory.add(id, if (amount == "max") Int.MAX_VALUE else amount.toSILong().toInt())
    }
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
    delay(player, 1) {
        player.clearVar<Skill>("skill_stat_flash")
    }
}

on<Command>({ prefix == "setlevel" }) { player: Player ->
    val split = content.split(" ")
    val skill = Skill.valueOf(split[0].capitalize())
    val level = split[1].toInt()
    val target = if (split.size > 2) {
        val name = content.removeSuffix("${split[0]} ${split[1]} ")
        players.indexed.first { it?.name.equals(name) }
    } else {
        player
    }
    player.experience.set(skill, Levels.getExperience(level).toDouble())
    if (level > 99) {
        player.levels.boost(skill, level - 99)
    }
    delay(player, 1) {
        player.clearVar<Skill>("skill_stat_flash")
    }
}

on<Command>({ prefix == "reset" }) { player: Player ->
    player.setVar("life_points", 100)
    for ((index, skill) in Skill.all.withIndex()) {
        player.levels.setOffset(skill, 0)
        player.experience.set(skill, Experience.defaultExperience[index])
    }
}

on<Command>({ prefix == "hide" }) { player: Player ->
    player.toggle("hidden")
}

on<Command>({ prefix == "skull" }) { player: Player ->
    player.skull()
}

on<Command>({ prefix == "unskull" }) { player: Player ->
    player.stop("skull")
}

on<Command>({ prefix == "rest" }) { player: Player ->
    player["energy"] = MAX_ENERGY
}

on<Command>({ prefix == "curses" }) { player: Player ->
    player.setVar(PRAYERS, if (player.isCurses()) "normal" else "curses")
}

on<Command>({ prefix == "pray" }) { player: Player ->
    player.levels.clearOffset(Skill.Prayer)
}

on<Command>({ prefix == "restore" }) { player: Player ->
    Skill.values().forEach {
        player.levels.clearOffset(it)
    }
}

on<Command>({ prefix == "sound" }) { player: Player ->
    val id = content.toIntOrNull()
    if (id == null) {
        player.playSound(content.toUnderscoreCase())
    } else {
        player.client?.playSoundEffect(id)
    }
}

on<Command>({ prefix == "midi" }) { player: Player ->
    val id = content.toIntOrNull()
    if (id == null) {
        player.playMidi(content.toUnderscoreCase())
    } else {
        player.client?.playMIDI(id)
    }
}

on<Command>({ prefix == "jingle" }) { player: Player ->
    val id = content.toIntOrNull()
    if (id == null) {
        player.playJingle(content.toUnderscoreCase())
    } else {
        player.client?.playJingle(id)
    }
}

on<Command>({ prefix == "song" || prefix == "track" }) { player: Player ->
    player.playTrack(content.toInt())
}

on<Command>({ prefix == "pos" || prefix == "mypos" }) { player: Player ->
    player.message(player.tile.toString())
    println(player.tile)
}

on<Command>({ prefix == "reload" }) { player: Player ->
    var reloadRegions = false
    when (content) {
        "stairs" -> get<Stairs>().load()
        "tracks", "songs" -> get<MusicTracks>().load()
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
        "nav graph", "ai graph" -> get<NavigationGraph>().load()
        "areas", "npcs", "floor items" -> {
            val areas: Areas = get()
            areas.load()
            areas.clear()
            reloadRegions = true
        }
        "object defs" -> get<ObjectDefinitions>().load()
        "anim defs", "anims" -> get<AnimationDefinitions>().load()
        "container defs", "containers" -> get<ContainerDefinitions>().load()
        "graphic defs", "graphics" -> get<GraphicDefinitions>().load()
        "npc defs" -> get<NPCDefinitions>().load()
        "item defs" -> get<ItemDefinitions>().load()
        "sound", "sounds", "sound effects" -> get<SoundDefinitions>().load()
        "midi" -> get<MidiDefinitions>().load()
        "music", "music effects", "jingles" -> get<JingleDefinitions>().load()
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
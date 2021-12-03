import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.removeVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.StackMode
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.sendContainer
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerLevels
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.tele
import world.gregs.voidps.engine.entity.definition.*
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionReader
import world.gregs.voidps.engine.map.spawn.ItemSpawns
import world.gregs.voidps.engine.map.spawn.NPCSpawns
import world.gregs.voidps.engine.utility.*
import world.gregs.voidps.network.encode.playJingle
import world.gregs.voidps.network.encode.playMIDI
import world.gregs.voidps.network.encode.playSoundEffect
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.isCurses
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.specialAttackEnergy
import world.gregs.voidps.world.interact.entity.player.effect.skull
import world.gregs.voidps.world.interact.entity.player.energy.MAX_RUN_ENERGY
import world.gregs.voidps.world.interact.entity.player.music.MusicTracks
import world.gregs.voidps.world.interact.entity.player.music.playTrack
import world.gregs.voidps.world.interact.entity.sound.playJingle
import world.gregs.voidps.world.interact.entity.sound.playMidi
import world.gregs.voidps.world.interact.entity.sound.playSound
import world.gregs.voidps.world.interact.world.Stairs
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

val areas: Areas by inject()
val players: Players by inject()

on<Command>({ prefix == "tele" || prefix == "tp" }) { player: Player ->
    if (content.contains(",")) {
        val params = content.split(",")
        val plane = params[0].toInt()
        val x = params[1].toInt() shl 6 or params[3].toInt()
        val y = params[2].toInt() shl 6 or params[4].toInt()
        player.tele(x, y, plane)
    } else {
        val parts = content.split(" ")
        val int = parts[0].toIntOrNull()
        when {
            int == null -> player.tele(areas.getValue(content).area)
            parts.size == 1 -> player.tele(Region(int).tile.add(32, 32))
            else -> player.tele(int, parts[1].toInt(), if (parts.size > 2) parts[2].toInt() else 0)
        }
    }
}

on<Command>({ prefix == "teletome" }) { player: Player ->
    val other = players.get(content) ?: return@on
    other.tele(player.tile)
}

on<Command>({ prefix == "teleto" }) { player: Player ->
    val other = players.get(content) ?: return@on
    player.tele(other.tile)
}

on<Command>({ prefix == "npc" }) { player: Player ->
    val id = content.toIntOrNull()
    val defs: NPCDefinitions = get()
    val npcs: NPCs = get()
    val npc = if (id != null) {
        npcs.add(defs.get(id).stringId, player.tile, Direction.NORTH)
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

val playerFactory: PlayerFactory by inject()

on<Command>({ prefix == "save" }) { _: Player ->
    players.forEach {
        playerFactory.save(it.name, it)
    }
}

val definitions: ItemDefinitions by inject()

on<Command>({ prefix == "item" }) { player: Player ->
    val parts = content.split(" ")
    val id = definitions.get(parts[0]).stringId
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
    val id = definitions.get(parts.first()).stringId
    val amount = parts[1]
    val name = content.removePrefix("${parts[0]} ${parts[1]} ")
    val target = players.get(name)
    if (target == null) {
        player.message("Couldn't find player $target")
    } else {
        target.inventory.add(id, if (amount == "max") Int.MAX_VALUE else amount.toSILong().toInt())
    }
}

on<Command>({ prefix == "find" }) { player: Player ->
    val search = content.toLowerCase()
    var found = false
    repeat(definitions.size) { id ->
        val def = definitions.getOrNull(id) ?: return@repeat
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
    for (skill in player.levels.offsets.keys) {
        if (player.levels.getOffset(skill) < 0) {
            player.levels.clearOffset(skill)
        }
    }
    player.setVar("life_points", 990)
    for (skill in Skill.all) {
        player.experience.set(skill, 14000000.0)
    }
    delay(player, 1) {
        player.clearVar("skill_stat_flash")
    }
}

on<Command>({ prefix == "setlevel" }) { player: Player ->
    val split = content.split(" ")
    val skill = Skill.valueOf(split[0].capitalize())
    val level = split[1].toInt()
    val target = if (split.size > 2) {
        val name = content.removeSuffix("${split[0]} ${split[1]} ")
        players.get(name)
    } else {
        player
    }
    if (target == null) {
        println("Unable to find target.")
    } else {
        target.experience.set(skill, PlayerLevels.getExperience(level).toDouble())
        player.levels.clearOffset(skill)
        delay(player, 1) {
            target.removeVar("skill_stat_flash", skill.name)
        }
    }
}

on<Command>({ prefix == "reset" }) { player: Player ->
    for ((index, skill) in Skill.all.withIndex()) {
        player.experience.set(skill, Experience.defaultExperience[index])
        player.levels.clearOffset(skill)
    }
    player.setVar(if (player.isCurses()) PrayerConfigs.QUICK_CURSES else PrayerConfigs.QUICK_PRAYERS, 0)
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
    player["energy"] = MAX_RUN_ENERGY
}

on<Command>({ prefix == "spec" }) { player: Player ->
    player.specialAttackEnergy = MAX_SPECIAL_ATTACK
}

on<Command>({ prefix.removeSuffix("s") == "curse" }) { player: Player ->
    player.setVar(PRAYERS, if (player.isCurses()) "normal" else "curses")
}

on<Command>({ prefix.removeSuffix("s") == "ancient" }) { player: Player ->
    player.open("ancient_spellbook")
}

on<Command>({ prefix.removeSuffix("s") == "lunar" }) { player: Player ->
    player.open("lunar_spellbook")
}

on<Command>({ prefix.removeSuffix("s") == "regular" || prefix.removeSuffix("s") == "modern" }) { player: Player ->
    player.open("modern_spellbook")
}

on<Command>({ prefix.removeSuffix("s") == "dung" || prefix.removeSuffix("s") == "dungeoneering" }) { player: Player ->
    player.open("dungeoneering_spellbook")
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
            get<ObjectDefinitions>().load()
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
        "areas", "npcs" -> {
            get<NPCDefinitions>().load()
            get<NPCSpawns>().load()
            get<Areas>().load()
            reloadRegions = true
        }
        "object defs" -> get<ObjectDefinitions>().load()
        "anim defs", "anims" -> get<AnimationDefinitions>().load()
        "container defs", "containers" -> get<ContainerDefinitions>().load()
        "graphic defs", "graphics", "gfx" -> get<GraphicDefinitions>().load()
        "npc defs" -> get<NPCDefinitions>().load()
        "item defs", "items", "floor items" -> {
            get<ItemSpawns>().load()
            get<ItemDefinitions>().load()
            reloadRegions = true
        }
        "sound", "sounds", "sound effects" -> get<SoundDefinitions>().load()
        "midi" -> get<MidiDefinitions>().load()
        "vars", "variables" -> get<VariableDefinitions>().load()
        "music", "music effects", "jingles" -> get<JingleDefinitions>().load()
        "interfaces" -> get<InterfaceDefinitions>().load()
        "spells" -> get<SpellDefinitions>().load()
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

on<Command>({ prefix == "debug" }) { player: Player ->
    player["debug"] = !player["debug", false]
}

val tables: DropTables by inject()

class ContainerDelegate(
    private val container: Container,
    private val list: MutableList<ItemDrop> = mutableListOf()
) : MutableList<ItemDrop> by list {
    override fun add(element: ItemDrop): Boolean {
        container.add(element.id, element.amount.random())
        return true
    }
}

on<Command>({ prefix == "sim" }) { player: Player ->
    val parts = content.split(" ")
    val name = parts.first()
    val count = parts.last().toSIInt()
    val table = tables.get(name) ?: tables.get("${name}_drop_table")
    val title = "${count.toSIPrefix()} '${name.removeSuffix("_drop_table")}' drop table rolls"
    if (table == null) {
        player.message("No drop table found for '$name'")
        return@on
    }
    if (count < 0) {
        player.message("Simulation count has to be more than 0.")
        return@on
    }
    if (count > 100000) {
        player.message("Calculating...")
    }
    val job = GlobalScope.async {
        val container = Container.setup(capacity = 40, id = "al_kharid_general_store", stackMode = StackMode.Always)
        coroutineScope {
            val time = measureTimeMillis {
                val divisor = 1000000
                val sections = count / divisor
                (0..sections)
                    .map {
                        async {
                            val temp = Container.setup(capacity = 40, stackMode = StackMode.Always)
                            val list = ContainerDelegate(temp)
                            for (i in 0L until if (it == sections) count.rem(divisor) else divisor) {
                                table.role(list = list)
                            }
                            temp
                        }
                    }.forEach {
                        it.await().moveAll(container)
                    }
            }
            if (time > 0) {
                val seconds = TimeUnit.MILLISECONDS.toSeconds(time)
                player.message("Simulation took ${if (seconds > 1) "${seconds}s" else "${time}ms"}")
            }
        }
        container.sortedByDescending { it.amount }
        container
    }
    player.action(ActionType.Shopping) {
        try {
            val container = await(job)
            var value = 0L
            for (item in container.getItems()) {
                if (item.isNotEmpty()) {
                    value += item.amount * item.def.cost.toLong()
                }
            }
            player.interfaces.open("shop")
            player.setVar("free_container", -1)
            player.setVar("main_container", 3)
            player.interfaceOptions.unlock("shop", "stock", 0 until container.capacity * 6, "Info")
            for ((index, item) in container.getItems().withIndex()) {
                player.setVar("amount_$index", item.amount)
            }
            player.sendContainer(container)
            player.interfaces.sendVisibility("shop", "store", false)
            player.interfaces.sendText("shop", "title", "$title - ${value.toDigitGroupString()}gp (${value.toSIPrefix()})")
            awaitInterface("shop")
        } finally {
            player.close("shop")
        }
    }
}
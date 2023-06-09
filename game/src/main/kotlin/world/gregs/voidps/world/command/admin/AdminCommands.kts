package world.gregs.voidps.world.command.admin

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.pearx.kasechange.toSentenceCase
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.bot.navigation.graph.NavigationGraph
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.chat.toSIInt
import world.gregs.voidps.engine.client.ui.chat.toSILong
import world.gregs.voidps.engine.client.ui.chat.toSIPrefix
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.contain.*
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.data.definition.extra.*
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.suspend.pauseForever
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
import world.gregs.voidps.world.interact.entity.player.effect.unskull
import world.gregs.voidps.world.interact.entity.player.energy.MAX_RUN_ENERGY
import world.gregs.voidps.world.interact.entity.player.music.MusicTracks
import world.gregs.voidps.world.interact.entity.sound.playJingle
import world.gregs.voidps.world.interact.entity.sound.playMidi
import world.gregs.voidps.world.interact.entity.sound.playSound
import world.gregs.voidps.world.interact.world.spawn.Stairs
import world.gregs.voidps.world.interact.world.spawn.loadNpcSpawns
import world.gregs.voidps.world.interact.world.spawn.loadObjectSpawns
import java.util.concurrent.TimeUnit
import kotlin.collections.set
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

on<Command>({ prefix == "teleto" }) { player: Player ->
    val target = players.firstOrNull { it.name.equals(content, true) }
    if (target != null) {
        player.tele(target.tile)
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
    val definition = if (id != null) defs.getOrNull(id) else defs.getOrNull(content)
    if (definition == null) {
        player.message("Unable to find npc with id ${content}.")
        return@on
    }
    val npcs: NPCs = get()
    println("""
        - name: $content
          x: ${player.tile.x}
          y: ${player.tile.y}
          plane: ${player.tile.plane}
    """.trimIndent())
    val npc = npcs.add(definition.stringId, player.tile, Direction.NORTH)
    npc?.start("movement_delay", -1)
}

on<Command>({ prefix == "save" }) { _: Player ->
    val account: PlayerFactory = get()
    players.forEach(account::queueSave)
}

val definitions: ItemDefinitions by inject()
val alternativeNames = mutableMapOf<String, String>()

on<World, Registered> {
    repeat(definitions.size) { id ->
        val definition = definitions.get(id)
        if (definition.has("aka")) {
            val list: List<String> = definition["aka"]
            for (name in list) {
                alternativeNames[name] = definition.stringId
            }
        }
    }
}

on<Command>({ prefix == "items" }) { player: Player ->
    val parts = content.split(" ")
    for (i in parts.indices) {
        val id = definitions.get(alternativeNames.getOrDefault(parts[i], parts[i])).stringId
        player.inventory.add(id)
    }
}

on<Command>({ prefix == "item" }) { player: Player ->
    val parts = content.split(" ")
    val id = definitions.get(alternativeNames.getOrDefault(parts[0], parts[0])).stringId
    val amount = parts.getOrNull(1) ?: "1"
    player.inventory.transaction { addToLimit(id, if (amount == "max") Int.MAX_VALUE else amount.toSILong().toInt()) }
    println(player.inventory.transaction.error)
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
    val search = content.lowercase()
    var found = false
    repeat(definitions.size) { id ->
        val def = definitions.getOrNull(id) ?: return@repeat
        if (def.name.lowercase().contains(search)) {
            player.message("[${def.name.lowercase()}] - id: $id", ChatType.Console)
            found = true
        }
    }
    if (!found) {
        player.message("No results found for '$search'", ChatType.Console)
    }
}

on<Command>({ prefix == "clear" }) { player: Player ->
    player.inventory.clear()
}

on<Command>({ prefix == "master" }) { player: Player ->
    for (skill in Skill.all) {
        player.experience.set(skill, 14000000.0)
        player.levels.restore(skill, 1000)
    }
    player.softQueue("", 1) {
        player.clear("skill_stat_flash")
    }
}

on<Command>({ prefix == "setlevel" }) { player: Player ->
    val split = content.split(" ")
    val skill = Skill.valueOf(split[0].toSentenceCase())
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
        target.experience.set(skill, PlayerLevels.getExperience(level, skill))
        player.levels.set(skill, level)
        player.softQueue("", 1) {
            target.removeVarbit("skill_stat_flash", skill.name.toSnakeCase())
        }
    }
}

on<Command>({ prefix == "reset" }) { player: Player ->
    for ((index, skill) in Skill.all.withIndex()) {
        player.experience.set(skill, Experience.defaultExperience[index])
        player.levels.set(skill, Levels.defaultLevels[index])
    }
    player[if (player.isCurses()) PrayerConfigs.QUICK_CURSES else PrayerConfigs.QUICK_PRAYERS] = emptyList<Any>()
    player["xp_counter"] = 0.0
    player.clearCamera()
}

on<Command>({ prefix == "hide" }) { player: Player ->
    player.appearance.hidden = !player.appearance.hidden
    player.flagAppearance()
}

on<Command>({ prefix == "skull" }) { player: Player ->
    player.skull()
}

on<Command>({ prefix == "unskull" }) { player: Player ->
    player.unskull()
}

on<Command>({ prefix == "rest" }) { player: Player ->
    player["energy"] = MAX_RUN_ENERGY
}

on<Command>({ prefix == "spec" }) { player: Player ->
    player.specialAttackEnergy = MAX_SPECIAL_ATTACK
}

on<Command>({ prefix.removeSuffix("s") == "curse" }) { player: Player ->
    player[PRAYERS] = if (player.isCurses()) "normal" else "curses"
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
    player.levels.clear(Skill.Prayer)
}

on<Command>({ prefix == "restore" }) { player: Player ->
    Skill.values().forEach {
        player.levels.clear(it)
    }
}

on<Command>({ prefix == "sound" }) { player: Player ->
    val id = content.toIntOrNull()
    if (id == null) {
        player.playSound(content.toSnakeCase())
    } else {
        player.client?.playSoundEffect(id)
    }
}

on<Command>({ prefix == "midi" }) { player: Player ->
    val id = content.toIntOrNull()
    if (id == null) {
        player.playMidi(content.toSnakeCase())
    } else {
        player.client?.playMIDI(id)
    }
}

on<Command>({ prefix == "jingle" }) { player: Player ->
    val id = content.toIntOrNull()
    if (id == null) {
        player.playJingle(content.toSnakeCase())
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
    when (content) {
        "stairs" -> get<Stairs>().load()
        "tracks", "songs" -> get<MusicTracks>().load()
        "objects" -> {
            val defs: ObjectDefinitions = get()
            val custom: GameObjects = get()
            defs.load()
            loadObjectSpawns(custom, definitions = defs)
        }
        "nav graph", "ai graph" -> get<NavigationGraph>().load()
        "npcs" -> {
            get<NPCDefinitions>().load()
            val npcs: NPCs = get()
            loadNpcSpawns(npcs)
        }
        "areas" -> get<Areas>().load()
        "object defs" -> get<ObjectDefinitions>().load()
        "anim defs", "anims" -> get<AnimationDefinitions>().load()
        "container defs", "containers" -> get<ContainerDefinitions>().load()
        "graphic defs", "graphics", "gfx" -> get<GraphicDefinitions>().load()
        "npc defs" -> get<NPCDefinitions>().load()
        "item on item", "item-on-item" -> {
            get<ItemOnItemDefinitions>().load()
        }
        "sound", "sounds", "sound effects" -> get<SoundDefinitions>().load()
        "midi" -> get<MidiDefinitions>().load()
        "vars", "variables" -> get<VariableDefinitions>().load()
        "music", "music effects", "jingles" -> get<JingleDefinitions>().load()
        "interfaces" -> get<InterfaceDefinitions>().load()
        "spells" -> get<SpellDefinitions>().load()
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
        val container = Container.debug(capacity = 40, id = "al_kharid_general_store")
        coroutineScope {
            val time = measureTimeMillis {
                val divisor = 1000000
                val sections = count / divisor
                (0..sections)
                    .map {
                        async {
                            val temp = Container.debug(capacity = 40)
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
    player.queue(name = "simulate drops") {
        try {
            val container = job.await()
            var value = 0L
            for (item in container.items) {
                if (item.isNotEmpty()) {
                    value += item.amount * item.def.cost.toLong()
                }
            }
            player.interfaces.open("shop")
            player["free_container"] = -1
            player["main_container"] = 3
            player.interfaceOptions.unlock("shop", "stock", 0 until container.size * 6, "Info")
            for ((index, item) in container.items.withIndex()) {
                player["amount_$index"] = item.amount
            }
            player.sendContainer(container)
            player.interfaces.sendVisibility("shop", "store", false)
            player.interfaces.sendText("shop", "title", "$title - ${value.toDigitGroupString()}gp (${value.toSIPrefix()})")
            pauseForever()
        } finally {
            player.close("shop")
        }
    }
}

fun Container.sortedByDescending(block: (Item) -> Int) {
    transaction {
        clear()
        items.sortedByDescending(block).forEachIndexed { index, item ->
            set(index, item)
        }
    }
}
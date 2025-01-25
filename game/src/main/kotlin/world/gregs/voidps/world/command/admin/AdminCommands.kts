@file:Suppress("UNCHECKED_CAST")

package world.gregs.voidps.world.command.admin

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.pearx.kasechange.toSentenceCase
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.bot.navigation.graph.NavigationGraph
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.*
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.event.modCommand
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.drop.DropTable
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.charge
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.network.login.protocol.encode.playJingle
import world.gregs.voidps.network.login.protocol.encode.playMIDI
import world.gregs.voidps.network.login.protocol.encode.playSoundEffect
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.world.activity.quest.Books
import world.gregs.voidps.world.activity.quest.quests
import world.gregs.voidps.world.activity.quest.refreshQuestJournal
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop
import world.gregs.voidps.world.interact.entity.obj.Teleports
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.PRAYERS
import world.gregs.voidps.world.interact.entity.player.combat.prayer.isCurses
import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import content.entity.player.effect.skull
import content.entity.player.effect.unskull
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.world.music.MusicTracks
import content.entity.world.music.MusicUnlock
import content.entity.sound.playJingle
import content.entity.sound.playMidi
import content.entity.sound.playSound
import content.entity.npc.spawn.loadNpcSpawns
import content.entity.obj.spawn.loadObjectSpawns
import java.util.concurrent.TimeUnit
import kotlin.collections.set
import kotlin.system.measureTimeMillis

val areas: AreaDefinitions by inject()
val players: Players by inject()

Command.adminCommands.add("${Colours.PURPLE.toTag()}====== Admin Commands ======</col>")
Command.adminCommands.add("")

adminCommand("tele (x) (y) [level]", "teleport to given coordinates or area name", listOf("tp")) {
    if (content.contains(",")) {
        val params = content.split(",")
        val level = params[0].toInt()
        val x = params[1].toInt() shl 6 or params[3].toInt()
        val y = params[2].toInt() shl 6 or params[4].toInt()
        player.tele(x, y, level)
    } else {
        val parts = content.split(" ")
        val int = parts[0].toIntOrNull()
        when {
            int == null -> when (content.lowercase()) {
                "draynor" -> player.tele(3086, 3248)
                "varrock" -> player.tele(3212, 3429)
                "lumbridge" -> player.tele(3222, 3219)
                else -> player.tele(areas[content])
            }
            parts.size == 1 -> player.tele(Region(int).tile.add(32, 32))
            else -> player.tele(int, parts[1].toInt(), if (parts.size > 2) parts[2].toInt() else 0)
        }
    }
}

adminCommand("teleto (player-name)", "teleport to another player") {
    val target = players.firstOrNull { it.name.equals(content, true) }
    if (target != null) {
        player.tele(target.tile)
    }
}

adminCommand("teletome (player-name)", "teleport another player to you") {
    val other = players.get(content) ?: return@adminCommand
    other.tele(player.tile)
}

adminCommand("npc (npc-id)", "spawn an npc") {
    val id = content.toIntOrNull()
    val defs: NPCDefinitions = get()
    val definition = if (id != null) defs.getOrNull(id) else defs.getOrNull(content)
    if (definition == null) {
        player.message("Unable to find npc with id ${content}.")
        return@adminCommand
    }
    val npcs: NPCs = get()
    println(
        """
        - name: $content
          x: ${player.tile.x}
          y: ${player.tile.y}
          level: ${player.tile.level}
    """.trimIndent()
    )
    val npc = npcs.add(definition.stringId, player.tile, Direction.NORTH)
    npc?.start("movement_delay", -1)
}

modCommand("save", "save all players") {
    val account: SaveQueue = get()
    players.forEach(account::save)
}

val definitions: ItemDefinitions by inject()
val alternativeNames = Object2ObjectOpenHashMap<String, String>()

worldSpawn {
    for (id in 0 until definitions.size) {
        val definition = definitions.get(id)
        val list = (definition.extras as? MutableMap<String, Any>)?.remove("aka") as? List<String> ?: continue
        for (name in list) {
            alternativeNames[name] = definition.stringId
        }
    }
}

adminCommand("items (item-id) [item-id] [item-id]...", "spawn multiple items at once") {
    val parts = content.split(" ")
    for (i in parts.indices) {
        val id = definitions.get(alternativeNames.getOrDefault(parts[i], parts[i])).stringId
        player.inventory.add(id)
    }
}

adminCommand("item (item-id) [item-amount]", "spawn an item by int or string id e.g. 'item pure_ess 2'") {
    val parts = content.split(" ")
    val definition = definitions.get(alternativeNames.getOrDefault(parts[0], parts[0]))
    val id = definition.stringId
    val amount = parts.getOrNull(1) ?: "1"
    val charges = definition.getOrNull<Int>("charges")
    player.inventory.transaction {
        if (charges != null) {
            for (i in 0 until amount.toSILong()) {
                val index = inventory.freeIndex()
                if (index == -1) {
                    break
                }
                set(index, Item(id, 1))
                if (charges > 0) {
                    charge(player, index, charges)
                }
            }
        } else {
            addToLimit(id, if (amount == "max") Int.MAX_VALUE else amount.toSILong().toInt())
        }
    }
    if (player.inventory.transaction.error != TransactionError.None) {
        player.message(player.inventory.transaction.error.toString())
    }
}

adminCommand("give (item-id) [amount] (player-name)", "spawn item in another players inventory") {
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

modCommand("find (item name)", "search the id of an item", aliases = listOf("search")) {
    val search = content.lowercase()
    var found = 0
    player.message("===== Items =====", ChatType.Console)
    found += search(player, get<ItemDefinitions>(), search) { it.name }
    player.message("===== Objects =====", ChatType.Console)
    found += search(player, get<ObjectDefinitions>(), search) { it.name }
    player.message("===== NPCs =====", ChatType.Console)
    found += search(player, get<NPCDefinitions>(), search) { it.name }
    player.message("$found results found for '$search'", ChatType.Console)
}

val utf8Regex = "[^\\x20-\\x7e]".toRegex()

fun <T> search(player: Player, definitions: DefinitionsDecoder<T>, search: String, getName: (T) -> String): Int where T : Definition, T : Extra {
    var found = 0
    for (id in definitions.definitions.indices) {
        val def = definitions.getOrNull(id) ?: continue
        val name = getName(def)
        if (name.lowercase().contains(search)) {
            player.message("[${name.lowercase().replace(utf8Regex, "")}] - id: $id${if (def.stringId.isNotBlank()) " (${def.stringId})" else ""}", ChatType.Console)
            found++
        }
    }
    return found
}

modCommand("clear", "delete all items in the players inventory") {
    player.inventory.clear()
}

adminCommand("master", "set all skills to 99") {
    for (skill in Skill.all) {
        player.experience.set(skill, 14000000.0)
        player.levels.restore(skill, 1000)
    }
    player.softQueue("", 1) {
        player.clear("skill_stat_flash")
    }
}

adminCommand("unlock [activity-type]", "unlock everything or of a type (music, tasks, emotes, quests)") {
    val type = content
    if (type == "" || type == "music" || type == "songs" || type == "music tracks" || type == "music_tracks") {
        get<EnumDefinitions>().get("music_track_names").map?.keys?.forEach { key ->
            MusicUnlock.unlockTrack(player, key)
        }
        player.message("All songs unlocked.")
    }
    if (type == "" || type == "tasks" || type == "achievements") {
        for (struct in get<StructDefinitions>().definitions) {
            if (struct.stringId.endsWith("_task")) {
                player[struct.stringId] = true
            }
        }
        player.message("All tasks completed.")
    }
    if (type == "" || type == "emotes") {
        val defs = get<InterfaceDefinitions>()
        for (compId in 26..52) {
            if (compId == 39) {
                continue
            }
            val component = defs.getComponent("emotes", compId) ?: continue
            player["unlocked_emote_${component.stringId}"] = true
        }
        player["unlocked_emote_lost_tribe"] = true
        player.message("All emotes unlocked.")
    }
    if (type == "" || type == "quests") {
        for (quest in quests) {
            player[quest] = "completed"
        }
        player["quest_points"] = player["quest_points_total", 1]
        player.refreshQuestJournal()
        player.message("All quests unlocked.")
    }
}

adminCommand("setlevel (skill-name) (level)", "set any skill to a specific level") {
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
        target.experience.set(skill, Level.experience(skill, level))
        player.levels.set(skill, level)
        player.softQueue("", 1) {
            target.removeVarbit("skill_stat_flash", skill.name.lowercase())
        }
    }
}

adminCommand("reset", "rest all skills to level 1") {
    for ((index, skill) in Skill.all.withIndex()) {
        player.experience.set(skill, Experience.defaultExperience[index])
        player.levels.set(skill, Levels.defaultLevels[index])
    }
    player[if (player.isCurses()) PrayerConfigs.QUICK_CURSES else PrayerConfigs.QUICK_PRAYERS] = emptyList<Any>()
    player["xp_counter"] = 0.0
    player.clearCamera()
}

modCommand("hide", "toggle invisibility to other players") {
    player.appearance.hidden = !player.appearance.hidden
    player.flagAppearance()
}

adminCommand("skull", "apply a skull to the player") {
    player.skull()
}

adminCommand("unskull", "remove a skull") {
    player.unskull()
}

adminCommand("rest", "set run energy to full") {
    player["energy"] = MAX_RUN_ENERGY
}

adminCommand("spec", "set special attack energy to full") {
    player.specialAttackEnergy = MAX_SPECIAL_ATTACK
}

adminCommand("curse", "toggle curse prayers", listOf("curses")) {
    player[PRAYERS] = if (player.isCurses()) "normal" else "curses"
}

adminCommand("ancient", "toggle ancient spellbook", listOf("ancients")) {
    player.open("ancient_spellbook")
}

adminCommand("lunar", "toggle lunar spellbook", listOf("lunars")) {
    player.open("lunar_spellbook")
}

adminCommand("regular", "toggle modern spellbook", listOf("regulars", "modern", "moderns")) {
    player.open("modern_spellbook")
}

adminCommand("dung", "toggle dungeoneering spellbook", listOf("dungs", "dungeoneering", "dungeoneerings")) {
    player.open("dungeoneering_spellbook")
}

adminCommand("pray", "restore full prayer points") {
    player.levels.clear(Skill.Prayer)
}

adminCommand("restore", "restore all skills") {
    Skill.entries.forEach {
        player.levels.clear(it)
    }
}

adminCommand("sound (sound-id)", "play a sound by int or string id") {
    val id = content.toIntOrNull()
    if (id == null) {
        player.playSound(content.toSnakeCase())
    } else {
        player.client?.playSoundEffect(id)
    }
}

adminCommand("midi (midi-id)", "play a midi effect by int or string id") {
    val id = content.toIntOrNull()
    if (id == null) {
        player.playMidi(content.toSnakeCase())
    } else {
        player.client?.playMIDI(id)
    }
}

adminCommand("jingle (jingle-id)", "play a jingle sound by int or string id") {
    val id = content.toIntOrNull()
    if (id == null) {
        player.playJingle(content.toSnakeCase())
    } else {
        player.client?.playJingle(id)
    }
}

val enums: EnumDefinitions by inject()

adminCommand("song (song-id)", "play a song by int id", listOf("track")) {
    val names = enums.get("music_track_names").map!!
    var id = content.toIntOrNull()
    if (id == null) {
        val search = content.replace(" ", "_")
        for ((key, value) in names) {
            if ((value as String).toSnakeCase() == search) {
                id = key
                break
            }
        }
        if (id != null) {
            player.playTrack(id)
        } else {
            player.message("Song not found with id '${search}'.")
        }
    } else {
        player.playTrack(content.toInt())
    }
}

modCommand("pos", "print out players current coordinates", listOf("mypos")) {
    player.message("${player.tile} Zone(${player.tile.zone.id}) ${player.tile.region}")
    println(player.tile)
}

adminCommand("reload (config-name)", "reload any type of content or file e.g. npcs, object defs, or settings") {
    when (content) {
        "book", "books" -> get<Books>().load()
        "stairs", "tele", "teles", "teleports" -> get<Teleports>().load()
        "tracks", "songs" -> get<MusicTracks>().load()
        "objects", "objs" -> {
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
        "areas" -> get<AreaDefinitions>().load()
        "object defs" -> get<ObjectDefinitions>().load()
        "emotes", "render anims", "render emotes" -> get<RenderEmoteDefinitions>().load()
        "anim defs", "anims" -> get<AnimationDefinitions>().load()
        "container defs", "containers", "inventory defs", "inventories", "inv defs", "invs" -> get<InventoryDefinitions>().load()
        "graphic defs", "graphics", "gfx" -> get<GraphicDefinitions>().load()
        "npc defs" -> get<NPCDefinitions>().load()
        "item on item", "item-on-item" -> {
            get<ItemOnItemDefinitions>().load()
        }
        "sound", "sounds", "sound effects" -> get<SoundDefinitions>().load()
        "quest", "quests" -> get<QuestDefinitions>().load()
        "midi" -> get<MidiDefinitions>().load()
        "vars", "variables" -> get<VariableDefinitions>().load()
        "music", "music effects", "jingles" -> get<JingleDefinitions>().load()
        "interfaces" -> get<InterfaceDefinitions>().load()
        "spells" -> get<SpellDefinitions>().load()
        "patrols", "paths" -> get<PatrolDefinitions>().load()
        "prayers" -> get<PrayerDefinitions>().load()
        "drops" -> get<DropTables>().load()
        "cs2", "cs2s", "client scripts" -> get<ClientScriptDefinitions>().load()
        "settings", "setting", "game setting", "game settings", "games settings", "properties", "props" -> {
            Settings.load()
            World.emit(SettingsReload)
        }
    }
}

adminCommand("shop (shop-id)", "open a shop by id") {
    player.emit(OpenShop(content))
}

adminCommand("debug", "toggle debug mode and printing logs") {
    val target = if (content.isNotEmpty()) {
        players.get(content)
    } else {
        player
    }
    if (target == null) {
        player.message("Unable to find player with name '$content'.")
        return@adminCommand
    }
    target["debug"] = !target["debug", false]
    player.message("Debugging ${if (target["debug", false]) "enabled" else "disabled"} for player '${target.name}'.")
}

val tables: DropTables by inject()

class InventoryDelegate(
    private val inventory: Inventory,
    private val list: MutableList<ItemDrop> = mutableListOf()
) : MutableList<ItemDrop> by list {
    override fun add(element: ItemDrop): Boolean {
        inventory.add(element.id, element.amount.random())
        return true
    }
}

modCommand("chance (drop-table-id)", "get the chances for all items of a drop table") {
    val table = tables.get(content) ?: tables.get("${content}_drop_table")
    if (table == null) {
        player.message("No drop table found for '$content'")
        return@modCommand
    }
    sendChances(player, table)
}

fun sendChances(player: Player, table: DropTable) {
    for (index in table.drops.indices) {
        val drop = table.drops[index]
        if (drop is ItemDrop) {
            val (item, chance) = table.chance(index) ?: continue
            val amount = when {
                item.amount.first == item.amount.last && item.amount.first > 1 -> "(${item.amount.first})"
                item.amount.first != item.amount.last && item.amount.first > 1 -> "(${item.amount.first}-${item.amount.last})"
                else -> ""
            }
            player.message("${item.id} $amount - 1/${chance.toInt()}")
        } else if (drop is DropTable) {
            sendChances(player, drop)
        }
    }
}

modCommand("sim (drop-table-name) (drop-count)", "simulate any amount of drops from a drop-table/boss") {
    val parts = content.split(" ")
    val name = parts.first()
    val count = parts.last().toSIInt()
    val table = tables.get(name) ?: tables.get("${name}_drop_table")
    val title = "${count.toSIPrefix()} '${name.removeSuffix("_drop_table")}' drops"
    if (table == null) {
        player.message("No drop table found for '$name'")
        return@modCommand
    }
    if (count < 0) {
        player.message("Simulation count has to be more than 0.")
        return@modCommand
    }
    player.message("Simulating $title")
    if (count > 100_000) {
        player.message("Calculating...")
    }
    GlobalScope.launch {
        val inventory = Inventory.debug(capacity = 100, id = "")
        coroutineScope {
            val time = measureTimeMillis {
                (0 until count).chunked(1_000_000).map { numbers ->
                    async {
                        val temp = Inventory.debug(capacity = 100)
                        val list = InventoryDelegate(temp)
                        for (i in numbers) {
                            table.role(list = list, members = true)
                        }
                        temp
                    }
                }.forEach {
                    it.await().moveAll(inventory)
                }
            }
            if (time > 0) {
                val seconds = TimeUnit.MILLISECONDS.toSeconds(time)
                player.message("Simulation took ${if (seconds > 1) "${seconds}s" else "${time}ms"}")
            }
        }
        val alch: (Item) -> Long = {
            it.amount * it.def.cost.toLong()
        }
        val exchange: (Item) -> Long = {
            it.amount * it.def["price", it.def.cost].toLong()
        }
        val sortByPrice = false
        try {
            if (sortByPrice) {
                inventory.sortedByDescending { exchange(it) }
            } else {
                inventory.sortedByDescending { it.amount.toLong() }
            }
            World.queue("drop_sim") {
                var alchValue = 0L
                var exchangeValue = 0L
                for (item in inventory.items) {
                    if (item.isNotEmpty()) {
                        alchValue += alch(item)
                        exchangeValue += exchange(item)
                        val (drop, chance) = table.chance(item.id) ?: continue
                        player.message("${item.id} 1/${(count / (item.amount / drop.amount.first.toDouble())).toInt()} (1/${chance.toInt()} real)")
                    }
                }
                player.message("Alch price: ${alchValue.toDigitGroupString()}gp (${alchValue.toSIPrefix()})")
                player.message("Exchange price: ${exchangeValue.toDigitGroupString()}gp (${exchangeValue.toSIPrefix()})")
                player.interfaces.open("shop")
                player["free_inventory"] = -1
                player["main_inventory"] = 510
                player.interfaceOptions.unlock("shop", "stock", 0 until inventory.size * 6, "Info")
                for ((index, item) in inventory.items.withIndex()) {
                    player["amount_$index"] = item.amount
                }
                player.sendInventory(inventory, id = 510)
                player.interfaces.sendVisibility("shop", "store", false)
                player.interfaces.sendText("shop", "title", "$title - ${alchValue.toDigitGroupString()}gp (${alchValue.toSIPrefix()})")
            }
        } catch (e: Exception) {
            player.close("shop")
        }
    }
}

fun Inventory.sortedByDescending(block: (Item) -> Long) {
    transaction {
        val items = items.clone()
        clear()
        items.sortedByDescending(block).forEachIndexed { index, item ->
            set(index, item)
        }
    }
}
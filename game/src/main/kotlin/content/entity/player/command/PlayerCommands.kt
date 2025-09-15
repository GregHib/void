package content.entity.player.command

import content.entity.combat.hit.damage
import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.skull
import content.entity.player.effect.unskull
import content.entity.player.modal.tab.Emotes
import content.entity.world.music.MusicUnlock
import content.quest.quests
import content.quest.refreshQuestJournal
import content.skill.prayer.PrayerConfigs.PRAYERS
import content.social.trade.exchange.GrandExchange
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.commandAlias
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.timer.TimerQueue
import kotlin.collections.iterator
import kotlin.getValue

@Script
class PlayerCommands {

    val players: Players by inject()
    val accounts: AccountDefinitions by inject()
    val exchange: GrandExchange by inject()
    val saveQueue: SaveQueue by inject()
    val enums: EnumDefinitions by inject()
    val structs: StructDefinitions by inject()
    val variables: VariableDefinitions by inject()

    init {
        modCommand("save", desc = "Save all players") { _, _ ->
            players.forEach(saveQueue::save)
            exchange.save()
        }

        adminCommand("skull", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Apply a skull to the player", handler = ::skull)
        adminCommand("unskull", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Remove skull from the player", handler = ::unskull)
        adminCommand("rest", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Set run energy to full", handler = ::rest)
        adminCommand("spec", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Set special attack energy to full", handler = ::specialRestore)
        adminCommand("pray", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Restore full prayer points", handler = ::prayerRestore)
        adminCommand("restore", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Restore all skills", handler = ::restore)
        adminCommand("hide", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Toggle invisibility to other players", handler = ::hide)
        adminCommand("pos", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Position of the players", handler = ::position)
        commandAlias("pos", "mypos")
        adminCommand("chat", stringArg("message", desc = "Text to display (use quotes for spaces)"), stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Force a chat message over a players head", handler = ::chat)
        adminCommand("hit", intArg("amount", desc = "Damage to deal", optional = true), stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Damage player by an amount", handler = ::hit)
        adminCommand("watch", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Look at another player", handler = ::watch)
        adminCommand("debug", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Toggle debug mode and logs", handler = ::debug)
        val activities = setOf("all", "music", "songs", "music_tracks", "tasks", "achievements", "emotes", "quests")
        adminCommand(
            "unlock",
            stringArg("activity-type", desc = "Type of activity", autofill = activities),
            stringArg("player-name", optional = true, autofill = accounts.displayNames.keys),
            desc = "Unlock all content for an activity for the player",
            handler = ::unlock,
        )
        val spellbooks = setOf("ancient", "lunar", "modern", "dungeoneering")
        adminCommand("spellbook", stringArg("spellbook-type", autofill = spellbooks, optional = true), stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Switch spellbook", handler = ::spellbook)
        val prayers = setOf("normal", "curses")
        adminCommand("prayers", stringArg("prayer-type", autofill = prayers, optional = true), stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Switch prayers", handler = ::prayers)
        adminCommand(
            "variables",
            stringArg("var-name", desc = "The variable name to search for", optional = true, autofill = variables.definitions.keys),
            stringArg("player-name", optional = true, autofill = accounts.displayNames.keys),
            desc = "Search the players set variables",
            handler = ::listVariables,
        )
        adminCommand(
            "timers",
            stringArg("timer-name", desc = "The timer name to search for", optional = true, autofill = variables.definitions.keys),
            stringArg("player-name", optional = true, autofill = accounts.displayNames.keys),
            desc = "Search the players active timers",
            handler = ::listTimers,
        )
        val types = setOf("all", "objects", "players", "npcs", "items", "collisions")
        adminCommand(
            "under",
            stringArg("entity-type", desc = "The type of entity to search for", optional = true, autofill = types),
            stringArg("player-name", optional = true, autofill = accounts.displayNames.keys),
            desc = "List entity types under the players",
            handler = ::listEntities,
        )
    }

    fun rest(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        target["energy"] = MAX_RUN_ENERGY
    }

    fun specialRestore(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        target.specialAttackEnergy = MAX_SPECIAL_ATTACK
    }

    fun prayerRestore(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        target.levels.clear(Skill.Prayer)
    }

    fun restore(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        for (skill in Skill.entries) {
            target.levels.clear(skill)
        }
    }

    fun skull(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        target.skull()
    }

    fun unskull(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        target.unskull()
    }

    fun hide(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        target.appearance.hidden = !target.appearance.hidden
        target.flagAppearance()
    }

    fun position(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        player.message("${target.tile} Zone(${target.tile.zone.id}) ${target.tile.region}")
        println(target.tile)
    }

    fun prayers(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(1)) ?: return
        target[PRAYERS] = args.getOrNull(0) ?: "prayers"
    }

    fun spellbook(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(1)) ?: return
        target.open("${args.getOrNull(0) ?: "modern"}_spellbook")
    }

    fun debug(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        target["debug"] = !target["debug", false]
        player.message("Debugging ${if (target["debug", false]) "enabled" else "disabled"} for player '${target.name}'.")
    }

    fun chat(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(1)) ?: return
        target.say(args[0])
    }

    fun hit(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(1)) ?: return
        target.damage(args.getOrNull(0)?.toIntOrNull() ?: 10)
    }

    fun watch(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0))
        if (target != null) {
            player.watch(target)
        } else {
            player.clearWatch()
        }
    }

    fun listVariables(player: Player, args: List<String>) {
        if (player.hasClock("search_delay")) {
            return
        }
        player.start("search_delay", 1)
        val target = players.find(player, args.getOrNull(1)) ?: return
        val search = args.getOrNull(0)
        player.message("=== Variables ===", ChatType.Console)
        for ((variable, value) in (target.variables as PlayerVariables).temp.toSortedMap()) {
            if (search != null && !variable.contains(search, ignoreCase = true)) {
                continue
            }
            player.message("$variable: $value", ChatType.Console)
        }
        player.message("=== Persistent Variables ===", ChatType.Console)
        for ((variable, value) in target.variables.data.toSortedMap()) {
            if (search != null && !variable.contains(search, ignoreCase = true)) {
                continue
            }
            player.message("$variable: $value", ChatType.Console)
        }
    }

    fun listTimers(player: Player, args: List<String>) {
        if (player.hasClock("search_delay")) {
            return
        }
        player.start("search_delay", 1)
        val target = players.find(player, args.getOrNull(1)) ?: return
        val search = args.getOrNull(0)
        player.message("=== Timers ===", ChatType.Console)
        for (timer in target.timers.queue) {
            if (search != null && !timer.name.contains(search, ignoreCase = true)) {
                continue
            }
            player.message("${timer.name}: ${timer.nextTick - GameLoop.tick}", ChatType.Console)
        }
        player.message("=== Soft Timers ===", ChatType.Console)
        for (timer in (target.softTimers as TimerQueue).queue) {
            if (search != null && !timer.name.contains(search, ignoreCase = true)) {
                continue
            }
            player.message("${timer.name}: ${timer.nextTick - GameLoop.tick}", ChatType.Console)
        }
    }

    fun listEntities(player: Player, args: List<String>) {
        if (player.hasClock("search_delay")) {
            return
        }
        player.start("search_delay", 1)
        val target = players.find(player, args.getOrNull(1)) ?: return
        val type = args.getOrNull(0)
        val tile = target.tile
        if (type == "all" || type == "objects") {
            val objs = get<GameObjects>()[tile]
            if (objs.isNotEmpty()) {
                player.message("--- Objects ---", ChatType.Console)
                for (obj in objs) {
                    player.message(obj.toString(), ChatType.Console)
                }
            }
        }
        if (type == "all" || type == "players") {
            val players = players[tile].filterNot { it == player }
            if (players.isNotEmpty()) {
                player.message("--- Players ---", ChatType.Console)
                for (other in players) {
                    player.message(other.toString(), ChatType.Console)
                }
            }
        }
        if (type == "all" || type == "npcs") {
            val npcs = get<NPCs>()[tile]
            if (npcs.isNotEmpty()) {
                player.message("--- NPCs ---", ChatType.Console)
                for (npc in npcs) {
                    player.message(npc.toString(), ChatType.Console)
                }
            }
        }
        if (type == "all" || type == "items") {
            val items = get<FloorItems>()[tile]
            if (items.isNotEmpty()) {
                player.message("--- Floor Items ---", ChatType.Console)
                for (item in items) {
                    player.message(item.toString(), ChatType.Console)
                }
            }
        }
        if (type == "all" || type == "collision") {
            val flag = get<Collisions>()[tile.x, tile.y, tile.level]
            if (flag != 0) {
                player.message("--- Collisions ---", ChatType.Console)
                player.message(flag.toString(), ChatType.Console)
            }
        }
    }

    fun unlock(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(1)) ?: return
        val type = args[0]
        if (type == "all" || type == "music" || type == "songs" || type == "music tracks" || type == "music_tracks") {
            enums.get("music_track_names").map?.keys?.forEach { key ->
                MusicUnlock.unlockTrack(target, key)
            }
            target.message("All songs unlocked.")
        }
        if (type == "all" || type == "tasks" || type == "achievements") {
            for (struct in structs.definitions) {
                if (struct.stringId.endsWith("_task")) {
                    target[struct.stringId] = true
                }
            }
            target.message("All tasks completed.")
        }
        if (type == "all" || type == "emotes") {
            for (component in Emotes.unlockableEmotes) {
                target["unlocked_emote_$component"] = true
            }
            target["unlocked_emote_lost_tribe"] = true
            target.message("All emotes unlocked.")
        }
        if (type == "all" || type == "quests") {
            for (quest in quests) {
                target[quest] = "completed"
            }
            target["quest_points"] = target["quest_points_total", 1]
            target.refreshQuestJournal()
            target.message("All quests unlocked.")
        }
    }
}

fun Players.find(player: Player, name: String?): Player? {
    if (name == null) {
        return player
    }
    val target = firstOrNull { it.name.equals(name, true) }
    if (target == null) {
        player.message("Unable to find player '$name' online.", ChatType.Console)
        return null
    }
    return target
}

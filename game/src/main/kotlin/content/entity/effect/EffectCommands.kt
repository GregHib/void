package content.entity.effect

import content.entity.effect.toxin.*
import content.entity.player.command.find
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.commandAlias
import world.gregs.voidps.engine.client.command.commandSuggestion
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players

class EffectCommands(
    val accounts: AccountDefinitions,
) : Script {

    init {
        adminCommand("disease", intArg("damage", optional = true), stringArg("player-name", optional = true), desc = "Disease the player", handler = ::disease)
        adminCommand("poison", intArg("damage", optional = true), stringArg("player-name", optional = true), desc = "Poison the player", handler = ::poison)
        adminCommand("freeze", intArg("ticks", optional = true), stringArg("player-name", optional = true), desc = "Freeze the player", handler = ::freeze)
        adminCommand("cure", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Cure the player", handler = ::cure)
        adminCommand("tfm", stringArg("npc-id", autofill = NPCDefinitions.ids.keys), stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Transform into an npc (-1 to clear)", handler = ::transform)
        commandAlias("tfm", "transform")
        commandSuggestion("tfm", "pnpc", "morph")
    }

    fun disease(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(1)) ?: return
        val damage = args.getOrNull(0)?.toIntOrNull() ?: 100
        if (player.diseased || damage < 0) {
            target.cureDisease()
        } else {
            target.disease(player, damage)
        }
    }

    fun poison(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(1)) ?: return
        val damage = args.getOrNull(0)?.toIntOrNull() ?: 100
        if (player.poisoned || damage < 0) {
            target.curePoison()
        } else {
            target.poison(player, damage)
        }
    }

    fun freeze(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(1)) ?: return
        val ticks = args.getOrNull(0)?.toIntOrNull() ?: 100
        if (player.poisoned || ticks < 0) {
            target.softTimers.clear("movement_delay")
        } else {
            target.freeze(ticks, force = true)
        }
    }

    fun transform(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(1)) ?: return
        target.transform(args[0])
    }

    fun cure(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(0)) ?: return
        target.curePoison()
        target.cureDisease()
        target.softTimers.clear("stunned")
        target.softTimers.clear("movement_delay")
        target.clearTransform()
    }
}

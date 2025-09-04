package content.skill.magic.book.modern

import content.skill.magic.spell.spell
import content.skill.prayer.protectMagic
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.type.CombatStage
import world.gregs.voidps.type.TimerState
import world.gregs.voidps.type.sub.Combat
import world.gregs.voidps.type.sub.TimerStart
import world.gregs.voidps.type.sub.TimerTick
import kotlin.math.sign

val Character.teleBlocked: Boolean get() = teleBlockCounter > 0

val Character.teleBlockImmune: Boolean get() = teleBlockCounter < 0

var Character.teleBlockCounter: Int
    get() = if (this is Player) get("teleport_block", 0) else this["teleport_block", 0]
    set(value) = if (this is Player) {
        set("teleport_block", value)
    } else {
        this["teleport_block"] = value
    }

fun Player.teleBlock(target: Character, ticks: Int) {
    if (target.teleBlocked) {
        message("This player is already effected by this spell.", ChatType.Filter)
        return
    }
    target.softTimers.start("teleport_block")
    target.teleBlockCounter = ticks
}

fun Character.teleBlockImmunity(minutes: Int) {
    softTimers.start("teleport_block")
}

fun Character.unblockTeleport() {
    softTimers.stop("teleport_block")
}

class TeleportBlock {

    @Combat(type = "magic", spell = "teleport_block", stage = CombatStage.PREPARE)
    fun prepare(player: Player, target: NPC): Boolean {
        player.message("You can't use that against an NPC.")
        return true
    }

    @TimerStart("teleport_block")
    fun start(player: Player, restart: Boolean): Int {
        if (player.teleBlockImmune) {
            return TimerState.CANCEL
        }
        if (player.protectMagic()) {
            player.teleBlockCounter /= 2
        }
        if (!restart) {
            player.message("You have been teleblocked.")
        }
        return 50
    }

    @TimerTick("teleport_block")
    fun tick(player: Player): Int {
        val blocked = player.teleBlocked
        player.teleBlockCounter -= player.teleBlockCounter.sign
        when (player.teleBlockCounter) {
            0 -> {
                if (blocked) {
                    player.message("Your teleblock has worn off.")
                } else {
                    player.message("Your teleblock resistance has worn off.")
                }
                return TimerState.CANCEL
            }
            -1 -> player.message("Your teleblock resistance is about to wear off.")
            1 -> player.message("Your teleblock is about to wear off.")
        }
        return TimerState.CONTINUE
    }
}

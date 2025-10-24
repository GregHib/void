package content.skill.magic.book.modern

import content.entity.combat.combatPrepare
import content.skill.magic.spell.spell
import content.skill.prayer.protectMagic
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.Timer
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

@Script
class TeleportBlock : Api {

    @Timer("teleport_block")
    override fun start(player: Player, timer: String, restart: Boolean): Int {
        if (player.teleBlockImmune) {
            return Timer.CANCEL
        }
        if (player.protectMagic()) {
            player.teleBlockCounter /= 2
        }
        if (!restart) {
            player.message("You have been teleblocked.")
        }
        return 50
    }

    @Timer("teleport_block")
    override fun tick(player: Player, timer: String): Int {
        val blocked = player.teleBlocked
        player.teleBlockCounter -= player.teleBlockCounter.sign
        when (player.teleBlockCounter) {
            0 -> {
                if (blocked) {
                    player.message("Your teleblock has worn off.")
                } else {
                    player.message("Your teleblock resistance has worn off.")
                }
                return Timer.CANCEL
            }
            -1 -> player.message("Your teleblock resistance is about to wear off.")
            1 -> player.message("Your teleblock is about to wear off.")
        }
        return Timer.CONTINUE
    }

    init {
        combatPrepare("magic") { player ->
            if (player.spell == "teleport_block" && target is NPC) {
                player.message("You can't use that against an NPC.")
                cancel()
            }
        }
    }
}

package content.skill.magic.book.modern

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

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

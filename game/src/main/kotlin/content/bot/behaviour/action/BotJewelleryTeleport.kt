package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import content.skill.magic.jewellery.itemTeleport
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment

/**
 * Bot-only jewellery teleport. Discharges one use of the equipped item, then runs the standard
 * jewellery teleport coroutine (sound/gfx/animation/tele/landing gfx). Forces past the combat
 * queue gate so a retreating bot can break out of an attack chain mid-fight.
 */
data class BotJewelleryTeleport(
    val item: String,
    val area: String,
    val condition: Condition? = null,
    val success: Condition? = null,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (condition != null && !condition.check(bot.player)) {
            return BehaviourState.Success
        }
        if (success != null && success.check(bot.player)) {
            return BehaviourState.Success
        }
        val player = bot.player
        // Teleport coroutine takes ~4 ticks (cast anim + tele + landing). Reactive ticks every
        // 1 tick — without this guard we'd stack multiple discharges + teleports per retreat.
        if (player.queue.contains("teleport_jewellery")) {
            return BehaviourState.Running
        }
        val slot = player.equipment.indexOf(item)
        if (slot < 0) {
            return BehaviourState.Failed(Reason.Invalid("$item not equipped for jewellery teleport."))
        }
        val areaDef = Areas.getOrNull(area)?.area ?: return BehaviourState.Failed(Reason.Invalid("Unknown area '$area'."))
        player.equipment.discharge(player, slot)
        itemTeleport(player, areaDef, "jewellery", force = true)
        return when {
            success == null -> BehaviourState.Wait(1, BehaviourState.Success)
            success.check(player) -> BehaviourState.Success
            else -> BehaviourState.Running
        }
    }
}

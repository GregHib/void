package content.bot.behaviour.condition

import content.entity.combat.attacking
import content.entity.combat.underAttack
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * The bot is swinging at something or being hit; used to keep offensive prayers and boosts to fights only.
 */
class BotFighting : Condition(1) {
    override fun keys() = emptySet<String>()
    override fun events() = emptySet<String>()
    override fun check(player: Player): Boolean = player.attacking || player.underAttack
}

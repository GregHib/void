package content.skill.constitution.drink

import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import content.skill.constitution.canConsume
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.*

@Script
class RecoverSpecial : Api {

    @Key("recover_special")
    override fun start(player: Player, timer: String, restart: Boolean) = 10

    @Key("recover_special")
    override fun tick(player: Player, timer: String): Int {
        if (player.dec("recover_special_delay") <= 0) {
            return Timer.CANCEL
        }
        return Timer.CONTINUE
    }

    @Key("recover_special")
    override fun stop(player: Player, timer: String, logout: Boolean) {
        player.clear("recover_special_delay")
    }

    init {
        canConsume("recover_special*") { player ->
            if (player.specialAttackEnergy == MAX_SPECIAL_ATTACK) {
                player.message("Drinking this would have no effect.")
                cancel()
            } else if (player.softTimers.contains("recover_special")) {
                player.message("You may only use this pot once every 30 seconds.")
                cancel()
            }
        }
    }
}

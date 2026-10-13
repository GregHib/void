package content.skill.construction

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

/**
 * Dining room bell-pulls for summoning the players servant
 * TODO servants
 */
class HouseDiningRoom : Script {
    init {
        objectOperate("Ring", "rope_bell_pull,bell_pull,posh_bell_pull") {
            anim("ring_bell_pull")
            message("The house has no servant.") // TODO proper message
        }
    }
}

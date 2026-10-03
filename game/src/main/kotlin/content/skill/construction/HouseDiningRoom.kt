package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Dining room bell-pulls for summoning the players servant
 */
class HouseDiningRoom : Script {
    init {
        objectOperate("Ring", "rope_bell_pull,bell_pull,posh_bell_pull") {
            notImplemented()
        }
    }
}

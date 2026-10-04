package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Combat room rings and the barriers and balance beams built inside them
 */
class HouseCombatRoom : Script {
    init {
        objectOperate("Climb-over", "boxing_ring,fencing_ring,combat_ring") {
            notImplemented()
        }

        objectOperate("Walk-through", "magic_barrier") {
            notImplemented()
        }

        objectOperate("Stand-on", BALANCE_BEAMS) {
            notImplemented()
        }

        objectOperate("Get-down", BALANCE_BEAMS) {
            notImplemented()
        }
    }

    companion object {
        private const val BALANCE_BEAMS = "balance_beam,balance_beam_end"
    }
}

package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Throne room levers for triggering traps and the trapdoors leading down to the oubliette
 */
class HouseThroneRoom : Script {
    init {
        objectOperate("Pull", LEVERS) {
            notImplemented()
        }

        objectOperate("Challenge-mode", LEVERS) {
            notImplemented()
        }

        objectOperate("Open", "oak_trapdoor,teak_trapdoor,mahogany_trapdoor") {
            notImplemented()
        }
    }

    companion object {
        private const val LEVERS = "oak_lever,teak_lever,mahogany_lever"
    }
}

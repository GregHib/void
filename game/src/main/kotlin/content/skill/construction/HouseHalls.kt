package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Skill hall head trophies and the quest hall's mounted amulet of glory
 */
class HouseHalls : Script {
    init {
        objectOperate("Talk-to", TROPHIES) {
            notImplemented()
        }

        objectOperate("Rub", "amulet_of_glory_mounted") {
            notImplemented()
        }
    }

    companion object {
        private const val TROPHIES = "crawling_hand_trophy,cockatrice_head_trophy,basilisk_head_trophy,kurask_head_trophy,abyssal_head_trophy,kbd_heads_trophy,kq_head_trophy"
    }
}

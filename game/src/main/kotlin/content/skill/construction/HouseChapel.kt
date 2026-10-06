package content.skill.construction

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.midi

/**
 * Chapel musical instruments
 */
class HouseChapel : Script {
    init {
        objectOperate("Play", "windchimes,bells") {
            anim("play_chimes")
            // TODO sounds
        }

        objectOperate("Play", "organ") {
            anim("play_organ")
            midi("church_organ")
            jingle("ambient_church_happy")
        }
    }
}

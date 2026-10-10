package content.skill.construction

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.midi
import world.gregs.voidps.engine.entity.character.sound

/**
 * Chapel musical instruments
 */
class HouseChapel : Script {
    init {
        objectOperate("Play", "windchimes") { (target) ->
            anim("human_use_windchimes")
            target.anim("musical_windchimes")
            sound("windchimes")
        }

        objectOperate("Play", "bells") { (target) ->
            anim("human_use_windchimes")
            target.anim("musical_bells")
            sound("bells")
        }

        objectOperate("Play", "organ") {
            anim("play_organ")
            midi("church_organ")
            jingle("ambient_church_happy")
        }
    }
}

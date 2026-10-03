package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Chapel musical instruments
 */
class HouseChapel : Script {
    init {
        objectOperate("Play", "windchimes,bells,organ") {
            notImplemented()
        }
    }
}

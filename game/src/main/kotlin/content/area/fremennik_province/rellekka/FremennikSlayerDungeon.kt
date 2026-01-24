package content.area.fremennik_province.rellekka

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script

class FremennikSlayerDungeon : Script {
    init {
        objectOperate("Read", "slayer_danger_sign") {
            statement("<red>WARNING!<br>This area contains very dangerous creatures!<br>Do not pass unless properly prepared!")
        }
    }
}
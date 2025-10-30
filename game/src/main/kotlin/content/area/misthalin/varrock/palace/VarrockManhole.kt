package content.area.misthalin.varrock.palace

import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace

class VarrockManhole : Script {

    init {
        objectOperate("Open", "varrock_manhole") { (target) ->
            target.replace("varrock_manhole_open")
            message("You pull back the cover from over the manhole.")
            sound("coffin_open")
        }

        objectOperate("Close", "varrock_manhole_open") { (target) ->
            target.replace("varrock_manhole")
            message("You place the cover back over the manhole.")
        }
    }
}

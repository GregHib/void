package world.gregs.voidps.world.map.varrock.palace

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.world.interact.entity.sound.playSound

objectOperate("Open", "varrock_manhole") {
    arriveDelay()
    target.replace("varrock_manhole_open")
    player.message("You pull back the cover from over the manhole.")
    player.playSound("coffin_open")
}

objectOperate("Close", "varrock_manhole_open") {
    arriveDelay()
    target.replace("varrock_manhole")
    player.message("You place the cover back over the manhole.")
}
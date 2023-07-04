package world.gregs.voidps.world.map.varrock.palace

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.world.interact.entity.sound.playSound

on<ObjectOption>({ operate && obj.id == "varrock_manhole" && option == "Open" }) { player: Player ->
    arriveDelay()
    obj.replace("varrock_manhole_open")
    player.message("You pull back the cover from over the manhole.")
    player.playSound("coffin_open")
}

on<ObjectOption>({ operate && obj.id == "varrock_manhole_open" && option == "Close" }) { player: Player ->
    arriveDelay()
    obj.replace("varrock_manhole")
    player.message("You place the cover back over the manhole.")
}
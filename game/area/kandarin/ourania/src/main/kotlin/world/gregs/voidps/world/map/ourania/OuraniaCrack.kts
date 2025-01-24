package world.gregs.voidps.world.map.ourania

import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.obj.objectOperate

objectOperate("Squeeze-through", "ourania_crack_enter") {
    player.open("fade_out")
    delay(3)
    player.tele(3312, 4817)
    delay(1)
    player.open("fade_in")
}

objectOperate("Squeeze-through", "ourania_crack_exit") {
    player.open("fade_out")
    delay(3)
    player.tele(3308, 4819)
    delay(1)
    player.open("fade_in")
}
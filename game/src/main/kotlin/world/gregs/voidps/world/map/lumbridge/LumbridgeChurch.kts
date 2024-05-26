package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.world.interact.entity.sound.playJingle
import world.gregs.voidps.world.interact.entity.sound.playMidi

objectOperate("Play", "lumbridge_organ") {
    player.setAnimation("play_organ")
    player.playMidi("church_organ")
    player.playJingle("ambient_church_happy")
    player["tinkle_the_ivories_task"] = true
}

objectApproach("Ring", "lumbridge_church_bell") {
    // TODO obj anim and sound
    player["ring_my_bell_task"] = true
}
package content.area.misthalin.lumbridge

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.npcOperate

npcOperate("Talk-to", "doomsayer") {
    npc<Talk>("Dooooom!")
    player<Scared>("Where?")
    npc<Talk>("All around us! I can feel it in the air, hear it on the wind, smell it... also in the air!")
    player<Scared>("Is there anything we can do about this doom?")
    npc<Talk>("There is nothing you need to do my friend! I am the Doomsayer, although my real title could be something like the Danger Tutor.")
    player<Quiz>("Danger Tutor?")
    npc<Happy>("Yes! I roam the world sensing danger.")
    npc<Talk>("If I find a dangerous area, the I put up warning signs that will tell you what is so dangerous about that area.")
    npc<Talk>("If you see the signs often enough, then you can turn them off; by that time you likely know what the area has in store for you.")
    player<Quiz>("But what if I want to see the warnings again?")
    npc<Happy>("That's why I'm waiting here!")
    player["doom_task"] = true
    if (player.variables.data.keys.none { it.startsWith("warning_") }) {
        npc<Talk>("If you want to see the warning messages again, I can turn them back on for you.")
        player<Happy>("Thanks, I'll remember that if I see any warning messages.")
        npc<Happy>("You're welcome!")
        return@npcOperate
    }
    npc<Quiz>("Do you need to turn on any warnings right now?")
    choice {
        option<Talk>("Yes, I do.") {
            player.open("doomsayer_warning_messages")
        }
        option<Talk>("Not right now.") {
            npc<Happy>("Ok, keep an eye out for the messages though!")
            player<Talk>("I will.")
        }
    }
}

npcOperate("Toggle-warnings", "doomsayer") {
    player.open("doomsayer_warning_messages")
}

interfaceOption("Toggle", "*", "doomsayer_warning_messages") {
    val count = player["warning_$component", 0]
    if (count < 6) {
        player.message("You cannot toggle this warning screen on or off.")
        player.message("You need to go to the area it is linked to enough times to have the option to do so.")
        return@interfaceOption
    }
    if (count == 6) {
        player["warning_$component"] = 7
    } else {
        player["warning_$component"] = 6
    }
}

interfaceOpen("warning_*") { player ->
    val count = player[id, 0]
    if (count < 6) {
        player[id] = count + 1
    }
}

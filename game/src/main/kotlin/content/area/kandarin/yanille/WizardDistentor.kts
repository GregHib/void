package content.area.kandarin.yanille

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import content.quest.questCompleted
import content.skill.runecrafting.EssenceMine
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player

npcOperate("Talk-to", "wizard_distentor") {
    npc<Talk>("Welcome to the Magicians' Guild!")
    if (!player.questCompleted("rune_mysteries")) {
        return@npcOperate
    }
    player<Talk>("Hello there.")
    npc<Quiz>("What can I do for you?")
    choice {
        option<Talk>("Nothing thanks, I'm just looking around.") {
            npc<Talk>("That's fine with me.")
        }
        option<Quiz>("Can you teleport me to the Rune Essence Mine?") {
            EssenceMine.teleport(target, player)
        }
    }
}

npcOperate("Teleport", "wizard_distentor") {
    if (player.questCompleted("rune_mysteries")) {
        EssenceMine.teleport(target, player)
    } else {
        player.message("You need to have completed the Rune Mysteries Quest to use this feature.")
    }
}

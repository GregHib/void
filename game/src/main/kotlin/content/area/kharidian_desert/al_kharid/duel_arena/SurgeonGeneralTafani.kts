package content.area.kharidian_desert.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.interact.dialogue.Happy
import world.gregs.voidps.world.interact.dialogue.Neutral
import world.gregs.voidps.world.interact.dialogue.Quiz
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "surgeon_general_tafani") {
    player<Happy>("Hi!")
    npc<Happy>("Hi. How can I help?")
    menu()
}

npcOperate("Heal", "surgeon_general_tafani") {
    heal()
}

suspend fun NPCOption<Player>.menu() {
    choice {
        option<Uncertain>("Can you heal me?") {
            heal()
        }
        fighters()
        often()
        option<Quiz>("Can you tell me about your cape?") {
            npc<Happy>("Certainly! Skillcapes are a symbol of achievement. Only people who have mastered a skill and reached level 99 can get their hands on them and gain the benefits they carry.")
            npc<Neutral>("The Cape of Constitution doubles the speed of your constitution replenishing when worn. Is there anything else I can help you with?")
            menu()
        }
    }
}
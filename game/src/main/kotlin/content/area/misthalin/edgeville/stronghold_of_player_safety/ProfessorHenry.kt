package content.area.misthalin.edgeville.stronghold_of_player_safety

import world.gregs.voidps.engine.Script
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral

class ProfessorHenry : Script {
    init {
        npcOperate("Talk-to", "professor_henry") {
            player<Happy>("Hello!")
            npc<Happy>("Good day!")
            npc<Neutral>("Did you want to know about the old jail block? We always need adventurers to keep the cockroaches in check.")
            player<Neutral>("Do I need to do your exam?")
            npc<Happy>("No, the exam isn't really for adventurers like you. Are you looking for the jail block?")
            player<Neutral>("Sure, tell me about the jail.")
            npc<Neutral>("In the cells downstairs there's a secret passage into the old prison. I hear that fame and fortune awaits a suitably skilled adventurer.")
            player<Happy>("Thanks, I'll look into that.")
        }
    }
}
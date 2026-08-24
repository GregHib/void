package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class TeacherAndPupil : Script {
    init {
        npcOperate("Talk-to", "teacher_and_pupil_2") {
            if (tile.region.id == 1764) {
                npc<Happy>("What do you want to see now, dear?")
                npc<Happy>("The penguins, the penguins!")
            } else if (tile.level == 2) {
                npc<Quiz>("That man over there talks funny, miss.")
                npc<Happy>("That's because he's an art critic, dear. They have some very funny ideas.")
            } else {
                npc<Angry>("Stop pulling, we've plenty of time to see everything.")
                npc<Happy>("Aww, but miss, it's sooo exciting.")
            }
        }

        npcOperate("Talk-to", "teacher_and_pupil") {
            npc<Shock>("Teacher! Sir! I need the toilet!")
            npc<Bored>("I told you to go before we got here.")
            npc<Shock>("But sir, I didn't need to go then!")
            npc<Bored>("Alright, come on then.")
        }
    }
}

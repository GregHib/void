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
            val teacher = "vm_female_school_teacher_dummy"
            val child = "vm_female_school_teacher_girl_dummy"
            if (tile.region.id == 1764) {
                npc<Happy>(teacher, "What do you want to see now, dear?")
                npc<Happy>(child, "The penguins, the penguins!")
            } else if (tile.level == 2) {
                npc<Quiz>(child, "That man over there talks funny, miss.")
                npc<Happy>(teacher, "That's because he's an art critic, dear. They have some very funny ideas.")
            } else {
                npc<Angry>(teacher, "Stop pulling, we've plenty of time to see everything.")
                npc<Happy>(child, "Aww, but miss, it's sooo exciting.")
            }
        }

        npcOperate("Talk-to", "teacher_and_pupil") {
            val teacher = "vm_male_school_teacher_chathead_dummy"
            val child = "vm_male_school_teacher_boy_chathead_dummy"
            npc<Shock>(child, "Teacher! Sir! I need the toilet!")
            npc<Bored>(teacher, "I told you to go before we got here.")
            npc<Shock>(child, "But sir, I didn't need to go then!")
            npc<Bored>(teacher, "Alright, come on then.")
        }
    }
}

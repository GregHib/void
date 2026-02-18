package content.area.misthalin.edgeville.stronghold_of_player_safety

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Blink
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class StrongholdOfPlayerSafetyStudents : Script {
    init {
        npcOperate("Talk-to", "student_barbarian_village") {
            player<Quiz>("How are you doing with the exam?")
            npc<Blink>("...")
            player<Confused>("That good, huh?")
            noTalking()
        }
        npcOperate("Talk-to", "student_barbarian_village_2") {
            player<Quiz>("How's the exam going at the moment?")
            npc<Quiz>("What exam?")
            player<Confused>("Uh...the exam you're taking?")
            npc<Happy>("I'm not taking an exam. I'm drawing pictures.")
            player<Confused>("Oh.I...er...see.")
            cheatingExam()
        }
        npcOperate("Talk-to", "student_barbarian_village_3") {
            npc<Confused>("What's 769 x 426?")
            player<Shock>("What? Er...I don't know.")
            npc<Sad>("Me neither.")
            noTalking()
        }
        npcOperate("Talk-to", "student_barbarian_village_4") {
            npc<Angry>("I broke my pencil. Do you have a spare one?")
            player<Sad>("No, I don't.")
            cheatingExam()
        }
        npcOperate("Talk-to", "student_edgeville") {
            npc<Angry>("Bah! I hate maths.")
            player<Confused>("I thought this was a Player Safety exam?")
            npc<Angry>("Yes, it is - but i just hate maths.")
            player<Confused>("Ah, okay.")
            cheatingExam()
        }
        npcOperate("Talk-to", "student_edgeville_2") {
            npc<Sad>("*Sob!* I'm going to fail! I hate exams.")
            player<Shock>("Cheer up! I'm sure you'll do fine. Try to concentrate.")
            noTalking()
        }
    }

    suspend fun Player.noTalking() {
        npc<Angry>("professor_henry", "No talking in the exam, please.")
        player<Shock>("Sorry!")
    }

    suspend fun Player.cheatingExam() {
        npc<Angry>("professor_henry", "Please leave the students alone while they take their exams. We don't want any cheating in here.")
        player<Shock>("Oh! Sorry about that.")
    }
}

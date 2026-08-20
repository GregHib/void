package content.area.misthalin.varrock.digsite

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Unamused
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script

class Elissa : Script {

    init {
        npcOperate("Talk-to", "elissa_digsite") {
            npc<Neutral>("Hello there.")
            choice {
                whatDoYouDoHere()
                whatIsThisPlace()
                if (questStage("the_golem") == 2) {
                    when (get("golem_b", 0)) {
                        1 -> foundLetter()
                        2 -> whereDidYouSayTheNotesWere()
                    }
                }
            }
        }
    }

    private fun ChoiceOption.whatDoYouDoHere() {
        option<Neutral>("What do you do here?") {
            npc<Neutral>("I'm helping with the dig. I'm an expert on Third Age architecture.")
        }
    }

    private fun ChoiceOption.whatIsThisPlace() {
        option("What is this place?") {
            player<Neutral>("What is this place?")
            npc<Shifty>("In the Third Age, this was a great city. Look at these giant walls! They put Varrock to shame!")
            choice {
                varrockImpressive()
                whatHappenedToTheCity()
            }
        }
    }

    private fun ChoiceOption.varrockImpressive() {
        option("I don't know, Varrock is pretty impressive.") {
            player<Neutral>("I don't know, Varrock is pretty impressive.")
            npc<Angry>("Hmph. I don't think it will look this good when it's been buried in the ground for three thousand years!")
        }
    }

    private fun ChoiceOption.whatHappenedToTheCity() {
        option("What happened to the city?") {
            player<Quiz>("What happened to the city?")
            npc<Shifty>("No one knows for sure.")
            npc<Neutral>("But the Third Age was a time of destruction, when the gods were violently at war.")
            npc<Sad>("Many great civilizations were destroyed then.")
        }
    }

    private fun ChoiceOption.foundLetter() {
        option("I found a letter in the desert with your name on.") {
            player<Quiz>("I found a letter in the desert with your name on.")
            npc<Neutral>("Ah, so you've found the ruins of Uzer.")
            npc<Sad>("I wrote that letter to my late husband when he was exploring there.")
            npc<Sad>("That was a great city as well, but the museum could only fund one excavation and this one was closer to home.")
            set("golem_b", 2)
            refreshQuestJournal()
            npc<Neutral>("If you're interested in his expedition, the notes he made are in the library in the Exam Centre.")
        }
    }

    private fun ChoiceOption.whereDidYouSayTheNotesWere() {
        option("Where did you say the notes were?") {
            player<Quiz>("Where did you say the notes were?")
            // RollEyes has no equivalent in this repo's Expression list.
            npc<Unamused>("They're on a bookcase in the Exam Centre.")
        }
    }
}

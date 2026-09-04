package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male

class FremennikWarrior : Script {
    init {
        npcOperate("Talk-to", "fremennik_warrior") {
            val title = if (male) "sir" else "ma'am"
            npc<Happy>("You, $title! You look like a capable adventurer. We need your help.")
            menu()
        }
    }

    private suspend fun Player.menu() {
        choice {
            whatDoYouNeed()
            whyDoYouNeedMe()
            soundsGood()
            tooBusy()
        }
    }

    private suspend fun Player.followUp() {
        choice {
            whyDoYouNeedMe()
            howDangerousIsThis()
            soundsGood()
            tooBusy()
        }
    }

    private fun ChoiceOption.whatDoYouNeed() {
        option("What do you need?") {
            player<Quiz>("What do you need?")
            npc<Happy>("My Fremennik clansmen and I have embarked on a test of skill and combat in the far north.")
            npc<Sad>("Unfortunately, it's proving more of a challenge than we had anticipated.")
            npc<Happy>("They need reinforcements to explore the dungeons there: to fight, or, if not, to provide support to those who do.")
            followUp()
        }
    }

    private fun ChoiceOption.whyDoYouNeedMe() {
        option("Why do you need me?") {
            val adventurer = if (male) "man" else "woman"
            val pronoun = if (male) "his" else "her"
            npc<Neutral>("You seem like a $adventurer with fire in $pronoun heart.")
            npc<Quiz>("A thirst for adventure, am I wrong?")
            followUp()
        }
    }

    private fun ChoiceOption.howDangerousIsThis() {
        option("How dangerous is this?") {
            player<Quiz>("How dangerous is this?")
            npc<Pleased>("There are challenges for adventurers of all skill-levels.")
            npc<Pleased>("My clansmen can explain more when you arrive.")
            followUp()
        }
    }

    private fun ChoiceOption.soundsGood() {
        option("Sounds good!") {
            player<Happy>("Sounds good!")
            npc<Happy>("Excellent! Just look for the jetty nearby. There, a boat will take you to your destination.")
        }
    }

    private fun ChoiceOption.tooBusy() {
        option("Sorry, I'm too busy.") {
            player<Neutral>("Sorry, I'm too busy.")
            npc<Neutral>("Suit yourself. We'll be here if you change your mind.")
        }
    }
}

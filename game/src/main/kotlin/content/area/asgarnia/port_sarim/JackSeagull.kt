package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.type.random

class JackSeagull : Script {

    init {
        npcOperate("Talk-to", "jack_seagull") {
            if (random.nextInt(4) == 0) {
                alternateDialogue()
                return@npcOperate
            }
            npc<Drunk>("Arrr, matey!")
            menu()
        }
    }

    private suspend fun Player.menu() = choice("Select an Option") {
        if (!questCompleted("pirates_treasure")) {
            option<Neutral>("I'm looking for Redbeard Frank.") {
                npc<Happy>("Redbeard Frank ye say? He be outside. Says he likes the feel of the wind on his cheeks.")
                player<Neutral>("Thanks.")
            }
        }
        option<Quiz>("What are you doing here?") {
            npc<Drunk>("Drinking.")
            player<Neutral>("Fair enough.")
        }
        option<Quiz>("Have you got any quests I could do?") {
            val piratesTreasure = questCompleted("pirates_treasure")
            val goblinDiplomacy = questCompleted("goblin_diplomacy")
            when {
                !piratesTreasure && !goblinDiplomacy -> {
                    npc<Happy>("Nay, but the barkeep hears most of the news around here. Or Redbeard Frank, he's often spoken of buried treasure. Perhaps ye should be asking him for quests.")
                }
                !piratesTreasure -> {
                    npc<Happy>("Nay, but Redbeard Frank has often spoken of buried treasure. Perhaps ye should be asking him for a quest.")
                }
                !goblinDiplomacy -> {
                    npc<Happy>("Nay, but the barkeep hears most of the news around here. Perhaps ye should be asking him for a quest.")
                }
                else -> {
                    npc<Happy>("Nay, I've nothing for ye to do. But I hear there's an old landlubber in Draynor Village who's always a-looking for a lively ${if (male) "lad" else "lass"} to do him a favour.")
                }
            }
            player<Neutral>("Thanks.")
        }
    }

    private suspend fun Player.alternateDialogue() {
        npc<Drunk>("Arrr, matey!")
        npc<Happy>("longbow_ben", "Yo ho ho!")
        player<Quiz>("So are you pirates?")
        npc<Happy>("longbow_ben", "Aye ${if (male) "laddie" else "lassie"}, that we are.")
        npc<Happy>("Aye, that we be.")
        npc<Angry>("longbow_ben", "Nay, always ye say it wrong! Tis 'we are', not 'we be'.")
        npc<Angry>("I be a pirate, not a scurvy schoolmaster!")
        npc<Angry>("longbow_ben", "Ye be a fool, and a disgrace to piracy.")
        npc<Laugh>("Now ye be saying 'be' too!")
        npc<Angry>("longbow_ben", "Arrr! Tis thy fault.")
        player<Neutral>("I think I'll leave you two to sort it out.")
    }
}

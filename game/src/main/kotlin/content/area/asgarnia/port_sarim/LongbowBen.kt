package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.type.random

class LongbowBen : Script {

    init {
        npcOperate("Talk-to", "longbow_ben") {
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
                npc<Neutral>("Redbeard Frank ye say? He be outside. Says he likes the feel of the wind on his cheeks.")
                player<Neutral>("Thanks.")
            }
        }
        option<Quiz>("Why are you called Longbow Ben?") {
            npc<Sad>("Arrr, that's a strange yarn.")
            npc<Neutral>("I was to be marooned, ye see. A scurvy troublemaker had taken my ship, and he put me ashore on a little island.")
            player<Quiz>("Gosh, how did you escape?")
            npc<Neutral>("Arrr, ye see, he made one mistake! Before he sailed away, he gave me a bow and one arrow so that I wouldn't have to die slowly.")
            npc<Laugh>("So I shot him and took my ship back.")
            player<Confused>("Right...")
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
        npc<Drunk>("jack_seagull", "Arrr, matey!")
        npc<Happy>("Yo ho ho!")
        player<Quiz>("So are you pirates?")
        npc<Happy>("Aye laddie, that we are.")
        npc<Happy>("jack_seagull", "Aye, that we be.")
        npc<Angry>("Nay, always ye say it wrong! Tis 'we are', not 'we be'.")
        npc<Angry>("jack_seagull", "I be a pirate, not a scurvy schoolmaster!")
        npc<Angry>("Ye be a fool, and a disgrace to piracy.")
        npc<Laugh>("jack_seagull", "Now ye be saying 'be' too!")
        npc<Angry>("Arrr! Tis thy fault.")
        player<Neutral>("I think I'll leave you two to sort it out.")
    }
}

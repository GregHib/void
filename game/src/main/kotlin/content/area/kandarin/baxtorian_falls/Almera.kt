package content.area.kandarin.baxtorian_falls

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Almera : Script {
    init {
        npcOperate("Talk-to", "almera_baxtorian_falls") {
            when (quest("waterfall_quest")) {
                "unstarted" -> start()
                "started" -> {
                    player<Happy>("Hello Almera.")
                    npc<Neutral>("Hello brave adventurer, have you seen my boy yet?")
                    player<Confused>("I'm afraid not, but I'm sure he hasn't gone far.")
                    npc<Confused>("I do hope so, you can't be too careful these days.")
                }
                "found_hudon" -> {
                    player<Happy>("Hello again.")
                    npc<Happy>("Well hello, you're still around then.")
                    player<Confused>("I saw Hudon by the river but he refused to come back with me.")
                    npc<Neutral>("Yes he told me, the foolish lad came in drenched to the bone, he had fallen into the waterfall, lucky he wasn't killed! Now he can spend the rest of the summer in his room.")
                    player<Happy>("Any ideas what I could do while I'm here?")
                    npc<Neutral>("Why don't you visit the tourist centre south of the waterfall?")
                }
                "read_book", "found_pebble" -> {
                    player<Happy>("Hello again Almera.")
                    npc<Happy>("Well hello again brave adventurer, are you enjoying the tranquil scenery of these parts?")
                    player<Happy>("Yes, very relaxing.")
                    npc<Neutral>("Well I'm glad to hear it The authorities wanted to dig up this whole area for a mine, but the few locals who lived here wouldn't budge and they gave up.")
                    player<Happy>("Good for you.")
                    npc<Happy>("Good for all of us!")
                }
                else -> {
                    player<Happy>("Hello Almera.")
                    npc<Happy>("Hello adventurer, how's your treasure hunt going?")
                    player<Neutral>("Oh, I'm just sight seeing.")
                    npc<Neutral>("No adventurer stays here this long just to sight see. But your business is yours alone, if you need to use the raft go ahead. But please try not crash it this time!")
                    player<Happy>("Thanks Almera.")
                }
            }
        }
    }

    private suspend fun Player.start() {
        player<Neutral>("Hello.")
        npc<Neutral>("Ah, hello there. Nice to see an outsider for a change, are you busy? I have a problem.")
        choice {
            option<Neutral>("How can I help?") {
                npc<Confused>("It's my son Hudon, he's always getting into trouble, the boy's convinced there's hidden treasure in the river and I'm a bit worried about his safety, the poor lad can't even swim.")
                player<Confused>("I could go and take a look for you if you like?")
                npc<Happy>("Would you? You are kind. You can use the small raft out back if you wish, do be careful, the current down stream is very strong.")
                set("waterfall_quest", "started")
            }
            option<Neutral>("I'm afraid I'm in a rush.") {
                npc<Confused>("Oh okay, never mind.")
            }
        }
    }
}

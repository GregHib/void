package content.area.kandarin.baxtorian_falls

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class Hudon : Script {
    init {
        npcApproach("Talk-to", "hudon_baxtorian_falls") {
            approachRange(5)
            if (tile.x !in 2510..2513 || tile.y !in 3476..3482) {
                message("Hudon can't hear you because of the noise of the waterfall. Perhaps the acoustics would be better from that island?")
                return@npcApproach
            }
            when (quest("waterfall_quest")) {
                "unstarted", "started" -> {
                    player<Neutral>("Hello son, are you okay? You need help?")
                    npc<Neutral>("It looks like you need the help.")
                    player<Neutral>("Your mum sent me to find you.")
                    npc<Neutral>("Don't play nice with me, I know you're looking for the treasure too.")
                    player<Neutral>("Where is this treasure you talk of?")
                    npc<Neutral>("Just because I'm small doesn't mean I'm dumb! If I told you, you would take it all for yourself.")
                    player<Neutral>("Maybe I could help.")
                    npc<Neutral>("I'm fine alone.")
                    message("Hudon is refusing to leave the waterfall.") // https://youtu.be/VhSmQKe_h0Q?t=32
                    if (quest("waterfall_quest") == "started") {
                        set("waterfall_quest", "found_hudon")
                    }
                }
                "found_hudon" -> {
                    player<Idle>("So you're still there.") // TODO expression
                    npc<Idle>("I'll find that treasure soon, just you wait and see.")
                }
                "read_book", "found_pebble", "found_amulet", "found_urn" -> {
                    player<Idle>("Hello Hudon.") // TODO expression
                    npc<Idle>("Oh it's you, trying to find my treasure again are you?")
                    player<Idle>("I didn't know it belonged to you.")
                    npc<Idle>("It will do when I find it. I just need to get into this blasted waterfall I've been washed downstream three times already.")
                }
                "entered_dungeon", "restored_amulet" -> {
                    player<Neutral>("Hello again.")
                    npc<Angry>("Not you still, why don't you give up?")
                    player<Quiz>("And miss all the fun!")
                    npc<Shifty>("You do understand that anything you find you have to share with me.")
                    player<Quiz>("Why's that?")
                    npc<Shifty>("Because I told you about the treasure.")
                    player<Happy>("Well, I wouldn't count on it.")
                    npc<Sad>("That's not fair.")
                    player<Neutral>("Neither is life, kid.")
                }
                else -> {
                    player<Quiz>("How are you doing, Hudon?")
                    npc<Sad>("No luck yet I'm afraid.")
                    player<Neutral>("Me neither, but I don't give up easily.")
                }
            }
        }
    }
}

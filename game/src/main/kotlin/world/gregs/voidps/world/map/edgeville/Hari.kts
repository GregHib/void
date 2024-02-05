package world.gregs.voidps.world.map.edgeville

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.minimumCanoeLevel

npcOperate("Talk-To", "hari") {
    player<Talking>("Hello there.")
    npc<Talking>("Hello.")
    choice {
        option<Talking>("Who are you?") {
            npc<Talking>("My name is Hari.")
            player<Unsure>("And what are you doing here Hari?")
            npc<Talking>("Like most people who come to Edgeville, I am here to seek adventure in the Wilderness.")
            npc<Talking>("I found a secret underground river that will take me quite a long way north.")
            player<Unsure>("Underground river? Where does it come out?")
            npc<Talking>("It comes out in a pond located deep in the Wilderness.")
            npc<Talking>("I had to find a very special type of canoe to get me up the river though, would you like to know more?")
            choice {
                option("Yes") {
                    canoeing()
                }
                option("No") {
                    player<Talking>("No thanks, not right now.")
                }
            }
        }
        option("Can you teach me about Canoeing?") {
            canoeing()
        }
    }
}

suspend fun CharacterContext.canoeing() {
    if (minimumCanoeLevel()) {
        return
    }
    npc<Talking>("It's really quite simple to make. Just walk down to that tree on the bank and chop it down.")
    npc<Talking>("When you have done that you can shape the log further with your axe to make a canoe.")
    when (player.levels.get(Skill.Woodcutting)) {
        in 12..26 -> {
            npc<Talking>("I can sense you're still a novice woodcutter, you will only be able to make a log canoe at present.")
            player<Unsure>("Is that good?")
            npc<Talking>("A log will take you one stop along the river. But you won't be able to travel into the Wilderness on it.")
        }
        in 27..41 -> {
            npc<Talking>("You are an average woodcutter. You should be able to make a Dugout canoe quite easily. It will take you 2 stops along the river.")
            player<Unsure>("Can I take a dugout canoe to reach the Wilderness?")
            npc<Laugh>("You would never make it there alive.")
            player<Sad>("Best not to try then.")
        }
        in 42..56 -> {
            npc<Talking>("You seem to be an accomplished woodcutter. You will easily be able to make a Stable Dugout")
            npc<Talking>("They are reliable enough to get you anywhere on this river, except to the Wilderness of course.")
            npc<Talking>("Only a Waka can take you there.")
            player<Unsure>("A Waka? What's that?")
            npc<Cheerful>("Come and ask me when you have improved your skills as a woodcutter.")
        }
        else -> {
            npc<Cheerful>("Your skills rival mine friend. You will certainly be able to build a Waka.")
            player<Unsure>("A Waka? What's that?")
            npc<Cheerful>("A Waka is an invention of my people, it's an incredible strong and fast canoe and will carry you safely to any destination on the river.")
            player<Unsure>("Any destination?")
            npc<Cheerful>("Yes, you can take a waka north through the underground portion of this river.")
            npc<Sad>("It will bring you out at a pond in the heart of the Wilderness. Be careful up there, many have lost more than their lives in that dark and twisted place.")
        }
    }
}
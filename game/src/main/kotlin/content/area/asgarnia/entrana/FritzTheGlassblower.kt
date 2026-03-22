package content.area.asgarnia.entrana

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script

class FritzTheGlassblower : Script {
    init {
        npcOperate("Talk-to", "fritz_the_glassblower") { (target) ->
            npc<Neutral>("Hello adventurer, welcome to the Entrana furnace.")
            npc<Quiz>("Would you like me to explain my craft to you?")
            choice {
                option<Neutral>("Yes please. I'd be fascinated to hear what you do.") {
                    npc<Happy>("I'm extremely pleased to hear that! I've always wanted an apprentice. Let me talk you through the secrets of glassblowing.")
                    npc<Happy>("Glass is made from soda ash and silica. We get our soda ash by collecting seaweed from the rocks - the prevailing currents make the north-west corner of the island the best place to find it, it can also be found in")
                    npc<Happy>("your nets sometimes when you're fishing, on Karamja island or at the Piscatoris Fishing Colony in the nets there. To turn seaweed into soda ash, all you need to do is burn it on a fire. Feel free to use the range in")
                    npc<Happy>("my house for that; it's the one directly west of here. Next we collect sand from the sandpit that you'll also find just west of here, there are others located in Yanille and Shilo Village.")
                    npc<Neutral>("You'll need a bucket to carry it in. Tell you what, you can have this old one of mine.")
                    addOrDrop("bucket")
                    npc<Neutral>("Bring the sand and the soda ash back here and melt them together in the furnace, and there you have it - molten glass!")
                    npc<Neutral>("There are many things you can use the molten glass for once you have made it. Depending on how talented you are, you could try turning it into something, like a fishbowl, for example. If you'd like to try your hand at")
                    npc<Neutral>("the fine art of glassblowing you can use my spare glassblowing pipe. I think I left it on the chest of drawers in my house this morning.")
                    npc<Neutral>("Alternatively I am always happy to buy the molten glass from you, saves me running about making it for myself.")
                    player<Neutral>("That sounds good. How much will you pay me?")
                    npc<Neutral>("Tell you what, because you've been interested in my art, I'll pay you the premium price of 20 gold pieces for each piece of molten glass you bring me.")
                }
                option("No thanks, I doubt I'll ever turn my hand to glassblowing.") {
                    player<Neutral>("No thanks, I doubt I'll ever turn my hand to glassblowing.")
                    npc<Sad>("Ok, suit yourself. Nobody seems to be interested in the skilled crafts these days.")
                }
            }
        }
    }
}

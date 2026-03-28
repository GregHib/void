package content.area.morytania.braindeath_island

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Despawn.Companion.player

class Brewer : Script {
    init {
        npcOperate("Talk-to", "brewer") {
            if (!questCompleted("rum_deal")) {
                return@npcOperate
            }
            player<Happy>("Hello there!")
            npc<Angry>("I don't know what your game is, but I know you're one of THEM!")
            player<Shock>("But I just saved you!")
            npc<Angry>("The voices tell me different. It's all part of a plot! Confess!")
        }

        npcOperate("Talk-to", "brewer_2") {
            if (!questCompleted("rum_deal")) {
                return@npcOperate
            }
            player<Happy>("Hello there!")
            npc<Scared>("Have they gone yet?")
            player<Neutral>("Well, no, but they are a lot calmer now.")
            npc<Scared>("What are we gonna do now, huh? What are we gunna do now?")
            player<Neutral>("In your case I would say relax.")
        }

        npcOperate("Talk-to", "brewer_3") {
            if (!questCompleted("rum_deal")) {
                return@npcOperate
            }
            player<Happy>("Hello there!")
            npc<Happy>("Hello yourself!")
            player<Quiz>("How's things?")
            npc<Shifty>("Fine...")
            player<Happy>("Excellent! Since I get the feeling I don't want to know why you said that so oddly I'll just go over here!")
            npc<Happy>("I think that would be for the best!")
        }

        npcOperate("Talk-to", "brewer_4") {
            if (!questCompleted("rum_deal")) {
                return@npcOperate
            }
            player<Happy>("Hello there!")
            npc<Happy>("Hello yerself Landlubber!")
            player<Quiz>("Everything ok with you now?")
            npc<Neutral>("Hmmm...Overall everything is good!")
            player<Happy>("Great!")
        }

        npcOperate("Talk-to", "brewer_5") {
            if (!questCompleted("rum_deal")) {
                return@npcOperate
            }
            player<Happy>("Hello there!")
            npc<Laugh>("Hello there, brave hero, in whom I had total confidence!")
            player<Quiz>("Total confidence?")
            npc<Happy>("Yes! I was so confident that I would never, ever have sold your soft, edible body to the pirates outside!")
            player<Shock>("Well, great...")
            npc<Shifty>("On a completely unrelated note, I would steer clear of Hungry Frank for a while.")
            npc<Angry>("He's a filthy liar. And a forger. It wouldn't surprise me if he has written out a note detailing the terms of our surrender and your dismemberment and cooking in MY handwriting.")
            npc<Shifty>("Imagine that, the fiend.")
        }

        npcOperate("Talk-to", "brewer_6") {
            if (!questCompleted("rum_deal")) {
                return@npcOperate
            }
            player<Quiz>("So...got any sleep yet?")
            npc<Drunk>("My brain is no longer capable of sleep.")
            player<Quiz>("So...what are you going to do now?")
            npc<Drunk>("I was gonna try and will myself dead.")
            player<Shifty>("Right...good luck with that.")
        }

        npcOperate("Talk-to", "brewer_7") {
            if (!questCompleted("rum_deal")) {
                return@npcOperate
            }
            player<Happy>("Hello there!")
            npc<Neutral>("Well you proved that you're probably not a zombie.")
            npc<Quiz>("So what are you then? A ghoul? A vampyre?")
            player<Angry>("I'm not any form of undead!")
            npc<Shock>("Oh...oh god I'm sorry, I didn't realise it was natural...")
            player<Quiz>("What?")
            npc<Shifty>("Nothing...")
        }

        npcOperate("Talk-to", "brewer_8") {
            if (!questCompleted("rum_deal")) {
                return@npcOperate
            }
            player<Happy>("Hello there!")
            npc<Happy>("You saved us! Huzzah!")
            player<Happy>("All in a day's work, think nothing of it.")
            npc<Happy>("I don't have anything to reward you with except my collection of bleak, gothic poetry I wrote when I assumed we were all done for. Do you want it?")
            player<Shifty>("I may come for it later, you hang on to it for now.")
        }
    }
}
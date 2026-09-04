package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Torrcs : Script {
    init {
        npcOperate("Talk-to", "torrcs") {
            player<Happy>("Hello.")
            npc<Neutral>("Hi there. So much to see here!")
            player<Laugh>("Yep, could be looking all day!")
            player<Quiz>("Updated any displays yet?")
            npc<Happy>("Yes, I just learned more about some priestly warriors and a staff of the gods.")
            player<Quiz>("Sounds fun, how did you do that?")
            npc<Neutral>("Ahh, I'm big on quests me, love questing. So when I've done one, I check here with 'Historian' Minas to see if I can help the Museum out with some information. Sometimes they already know it, or don't want to know,")
            npc<Neutral>("and sometimes it earns me a reward or two.")
            player<Happy>("That sounds really good. Perhaps I should do the same!")
            npc<Neutral>("You should, as well as everything else there is here in the Museum. Did you know they have a really fun- looking display down in the basement? I love monkeys.")
            player<Happy>("I'll check it out. Got to get going now, nice talking to you.")
            npc<Happy>("Have a good day!")
        }
    }
}

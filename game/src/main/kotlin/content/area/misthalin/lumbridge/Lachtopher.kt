package content.area.misthalin.lumbridge

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.RollEyes
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.event.Script

@Script
class Lachtopher {

    init {
        npcOperate("Talk-to", "lachtopher") {
            player<Happy>("Hello there.")
            npc<RollEyes>("Hello, I suppose. I'm Lachtopher. Could you lend me some money?")
            player<Quiz>("Lend you money? I really don't think so. Don't you have any of your own?")
            npc<RollEyes>("I spent it all and I can't be bothered to earn any more.")
            player<Quiz>("Right, and you want my hard-earned money instead? No chance!")
            npc<RollEyes>("You're just like my sister, Victoria. She won't give me any money.")
            player<Happy>("Your sister sounds like she has the right idea.")
            npc<RollEyes>("Yeah, I've heard it all before. 'Oh,' she says, 'It's easy to make money: just complete Tasks for cash.")
            player<Happy>("Well, if you want to make money...")
            npc<RollEyes>("That's just it. I don't want to make money. I just want to have money.")
            player<Angry>("I've had it with you! I don't think I've come across a less worthwhile person.")
            player<Angry>("I think I'll call you Lazy Lachtopher, from now on.")
        }
    }
}

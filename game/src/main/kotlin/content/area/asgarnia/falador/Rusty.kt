package content.area.asgarnia.falador

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Rusty : Script {

    init {
        npcOperate("Talk-to", "rusty") {
            npc<Happy>("Hiya. Are you carrying anything valuable?")
            player<Confused>("Why are you asking?")
            npc<Shifty>("Um... It's a quiz. I'm asking everyone I meet if they're carrying anything valuable.")
            player<Neutral>("What would you do if I said I had loads of expensive items with me?")
            npc<Happy>("Ooh, do you? It's been ages since anyone said they'd got anything worth stealing.")
            player<Angry>("'Anything worth stealing'?")
            npc<Confused>("Um... Not that I'd dream of stealing anything!")
            player<Shifty>("Well, I'll say I'm not carrying anything valuable at all.")
            npc<Sad>("Oh, what a shame.")
        }
    }
}

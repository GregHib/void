package content.area.asgarnia.falador

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class GuardFalador : Script {
    init {
        npcOperate("Talk-to", "guard_falador_7") {
            player<Quiz>("You're a long way out from the city.")
            npc<Neutral>("I know. We guards usually stay by banks and shops, but I got sent all the way out here to keep an eye on the brigands loitering just south of here.")
            player<Neutral>("It sounds more exciting than standing around guarding banks and shops.")
            npc<Happy>("It's not too bad. At least I don't get attacked so often out here. Guards in the cities get killed all the time.")
            player<Shifty>("Honestly, people these days just don't know how to behave!")
        }
    }
}

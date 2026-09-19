package content.area.asgarnia.falador

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class ManFalador : Script {
    init {
        npcOperate("Talk-to", "man_falador") {
            player<Happy>("Hello.")
            npc<Angry>("What are you doing in my house?")
            player<Happy>("I was just exploring.")
            npc<Confused>("You're exploring my house?")
            player<Happy>("You don't mind, do you?")
            npc<Confused>("But... why are you exploring my house?")
            player<Happy>("Oh, I don't know. I just wandered in, saw you and thought it'd be fun to speak to you.")
            npc<Confused>("... you are very strange...")
            player<Happy>("Perhaps I should go now.")
            npc<Sad>("Yes, please go away now.")
        }
    }
}

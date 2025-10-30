package content.area.wilderness

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class GhostlyPiper : Script {

    init {
        npcOperate("Talk-to", "ghostly_piper") {
            if (player.equipped(EquipSlot.Amulet).id != "ghostspeak_amulet") {
                npc<Pleased>("Woo, wooo. Woooo.")
                player.message("The ghost seems barely aware of your existence,")
                player.message("but you sense that resting here might recharge you for battle!")
                return@npcOperate
            }
            choice()
        }
    }

    suspend fun SuspendableContext<Player>.choice() {
        choice {
            option<Quiz>("Who are you?") {
                npc<Happy>("I play the pipes, to rouse the brave warriors of Saradomin for the fight!")
                player<Quiz>("Which fight?")
                npc<Happy>("Why, the great battles with the forces of Zamorak, of course!")
                player<Quiz>("I see. How long have you been standing here then?")
                npc<Happy>("Well, it is all a bit fuzzy. I remember standing at the front of the massed forces of Saradomin, and playing the Call to Arms, but after that I can't quite recall.")

                player<Talk>("I think you've been here for quite some time. You do know you're a gh-")
                player<Neutral>("No, never mind, you look happy enough here, and your music is quite rousing. I might rest here a while.")
                choice()
            }
            option<Quiz>("That's all for now") {
                npc<Happy>("Be strong and fight the good fight, my friend!")
            }
        }
    }
}

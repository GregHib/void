package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class SecurityGuardPortSarim : Script {

    init {
        npcOperate("Talk-to", "security_guard_port_sarim") {
            npc<Happy>("Hiya. I'm giving out free books that teach you how to keep your account secure.")
            when {
                inventory.contains("security_book") -> {
                    player<Neutral>("Got one already, thanks.")
                    npc<Happy>("Okay. Have a nice day.")
                }

                inventory.isFull() -> {
                    player<Neutral>("I don't have space to take anything from you at the moment.")
                    npc<Sad>("Fair enough")
                }

                inventory.add("security_book") -> {
                    item("security_book", "The Security Guard hands you a book about security.")
                    player<Confused>("Oh? Thanks.")
                    npc<Happy>("You're welcome.")
                }
            }
        }
    }
}

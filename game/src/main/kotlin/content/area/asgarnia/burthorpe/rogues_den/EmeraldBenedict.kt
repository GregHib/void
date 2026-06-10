package content.area.asgarnia.burthorpe.rogues_den

import content.entity.player.bank.pin.openBank
import content.entity.player.bank.pin.openCollection
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open

class EmeraldBenedict : Script {
    init {
        npcOperate("Talk-to", "emerald_benedict") {
            npc<Shifty>("Got anything you don't want to lose?")
            choice {
                option("Yes actually, can you help?", block = { openBank() })
                option("Yes, but can you show me my PIN settings?", block = { open("bank_pin_settings") })
                option("Yes, but I'd like to collect items now.", block = { openCollection() })
                option("Yes, but I'd like to see my Returned items now.", block = { open("returned_items") })
                option<Shock>("Yes thanks, and I'll keep hold of it too.")
            }
        }

        npcOperate("Bank", "emerald_benedict") {
            openBank()
        }

        npcOperate("Collect", "emerald_benedict") {
            openCollection()
        }
    }
}

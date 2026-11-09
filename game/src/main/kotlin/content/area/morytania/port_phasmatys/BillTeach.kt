package content.area.morytania.port_phasmatys

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class BillTeach : Script {
    init {
        // https://runescape.wiki/w/Transcript:Bill_Teach
        npcOperate("Talk-to", "bill_teach_port_phasmatys_2") {
            if (ownsItem("little_book_o_piracy")) {
                npc<Neutral>("If yer after a lift go to the ship and I'll take ye where ye want to go.")
                return@npcOperate
            }
            player<Sad>("Cap'n, I lost that book you gave me.")
            npc<Neutral>("Ye'll notice I'm not fallin' to the floor in shock right now.")
            npc<Neutral>("Look, if I gives ye another copy will ye keep a better eye on it?")
            player<Happy>("Aye Cap'n!")
            npc<Neutral>("Then here, have this other copy I dug up.")
            addOrDrop("little_book_o_piracy")
        }

        npcOperate("Travel", "bill_teach_port_phasmatys_2") {
            sail()
        }
    }

    private suspend fun Player.sail() {
        // Ship tiles from Bill Teach's carrier route (Mos Le'Harmless <-> Port Phasmatys)
        val destination = if (tile.y > 3200) Tile(3682, 2948, 1) else Tile(3714, 3499, 1)
        open("fade_out")
        delay(4)
        tele(destination)
        open("fade_in")
    }
}

package content.quest.member.ghosts_ahoy

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class GhostspeakAmulet : Script {
    init {
        playerSpawn {
            if (equipped(EquipSlot.Amulet).id.startsWith("ghostspeak_amulet")) {
                set("wearing_ghost_speak_amulet", true)
            }
        }

        itemAdded("ghostspeak_amulet*", "worn_equipment", EquipSlot.Amulet) {
            set("wearing_ghost_speak_amulet", true)
        }

        itemRemoved("ghostspeak_amulet*", "worn_equipment", EquipSlot.Amulet) {
            set("wearing_ghost_speak_amulet", false)
        }
    }
}

package content.area.karamja.shilo_village

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot


class Fernahei : Script {

    init {
        npcOperate("Talk-to", "fernahei_shilo_village") {
            npc<Happy>("Welcome to Fernahei's Fishing Shop Bwana! Would you like to see my items?")
            choice {
                option<Neutral>("Yes please!") {
                    openShop("fernaheis_fishing_hut")
                }
                option("No, but thanks for the offer.") {
                    npc<Happy>("That's fine and thanks for your interest.")
                }
            }
            npcOperate("Trade", "fernahei_shilo_village") {
                val amulet = equipped(EquipSlot.Amulet)
                if (amulet.id == "monkeyspeak_amulet") {
                    openShop("dagas_scimitar_smithy")
                } else {
                    npc<Shifty>("Ook! Ah Uh Ah! Ook Ook! Ah!")
                }
            }
        }
    }
}
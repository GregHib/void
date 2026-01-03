package content.area.morytania.mort_myre_swamp

import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class BillBlakey : Script {

    init {
        npcOperate("Talk-to", "bill_blakey") {
            if (equipped(EquipSlot.Amulet).id != "ghostspeak_amulet") {
                npc<Neutral>("Woo, wooo. Woooo.")
                message("The ghost seems barely aware of your existence,")
                message("but you sense that resting here might recharge you for battle!")
                return@npcOperate
            }
            npc<Neutral>("How sweet I roamed from fen to fen, And tasted all the Myre's pride, 'Till I the queen of love did ken, Who in the spirit beams did glide!")
            npc<Idle>("She shew'd me lilies in her hair, And blushing roses for her brow; She led me through her gardens fair, Where all her silver blooms do grow.")
            npc<Idle>("With sweet Myre dews my wings were wet, And Phoebus' kiss did slowly fade; She'd caught me in her silken net, And trap'd me as this silver shade.")
            npc<Idle>("She loves to sit and hear me sing, Then laughing, sports and plays with me; Then stretches out her silver wing, And mocks my loss of liberty.")
        }
    }
}

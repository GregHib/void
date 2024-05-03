package world.gregs.voidps.world.map.musicians

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.dialogue.Neutral
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.npc

npcOperate("Talk-to", "bill_blakey") {
    if (player.equipped(EquipSlot.Amulet).id != "ghostspeak_amulet") {
        npc<Talk>("Woo, wooo. Woooo.")
        player.message("The ghost seems barely aware of your existence,")
        player.message("but you sense that resting here might recharge you for battle!")
        return@npcOperate
    }
    npc<Talk>("How sweet I roamed from fen to fen, And tasted all the Myre's pride, 'Till I the queen of love did ken, Who in the spirit beams did glide!")
    npc<Neutral>("She shew'd me lilies in her hair, And blushing roses for her brow; She led me through her gardens fair, Where all her silver blooms do grow.")
    npc<Neutral>("With sweet Myre dews my wings were wet, And Phoebus' kiss did slowly fade; She'd caught me in her silken net, And trap'd me as this silver shade.")
    npc<Neutral>("She loves to sit and hear me sing, Then laughing, sports and plays with me; Then stretches out her silver wing, And mocks my loss of liberty.")
}
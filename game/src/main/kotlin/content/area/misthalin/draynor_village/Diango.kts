package content.area.misthalin.draynor_village

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.DiangoCodeDefinitions
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import content.entity.player.dialogue.Chuckle
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.stringEntry
import content.entity.npc.shop.openShop

npcOperate("Talk-to", "diango") {
    npc<Happy>("Howdy there partner! Want to see my spinning plates? Or did ya want a holiday item back?")
    choice {
        option<Quiz>("Spinning plates?") {
            npc<Happy>("That's right. There's a funny story behind them, their shipment was held up by thieves.")
            npc<Chuckle>("The crate was marked 'Dragon Plates'. Apparently they thought it was some kind of armour, when really it's just a plate with a dragon on it!")
            player.openShop("diangos_toy_store")
        }
        option<Neutral>("I'd like to check holiday items please!") {
            player.open("diangos_item_retrieval")
        }
        option<Quiz>("What else are you selling?") {
            player.openShop("diangos_toy_store")
        }
        option<Happy>("I'm fine, thanks.")
    }
}

npcOperate("Holiday-items", "diango") {
    player.open("diangos_item_retrieval")
}

val codeDefinitions: DiangoCodeDefinitions by inject()

npcOperate("Redeem-code", "diango") {
    val code = stringEntry("Please enter your code.").lowercase()
    val definition = codeDefinitions.getOrNull(code)
    if (definition == null) {
        player.message("Your code was not valid. Please check it and try again.")
        return@npcOperate
    }
    for (item in definition.add) {
        if (player[definition.variable, false]) {
            player.message("You have already claimed this code.")
            return@npcOperate
        }
    }
    val success = player.inventory.transaction {
        for (item in definition.add) {
            add(item.id, item.amount)
        }
    }
    if (success) {
        player[definition.variable] = true
        player.message("Your code has been successfully processed.")
    } else {
        player.inventoryFull()
    }
}
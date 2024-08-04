package world.gregs.voidps.world.map.draynor

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.DiangoCodeDefinitions
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.world.interact.dialogue.Chuckle
import world.gregs.voidps.world.interact.dialogue.Happy
import world.gregs.voidps.world.interact.dialogue.Neutral
import world.gregs.voidps.world.interact.dialogue.Quiz
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.stringEntry
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

npcOperate("Talk-to", "diango") {
    npc<Happy>("Howdy there partner! Want to see my spinning plates? Or did ya want a holiday item back?")
    choice {
        option<Quiz>("Spinning plates?") {
            npc<Happy>(" That's right. There's a funny story behind them, their shipment was held up by thieves.")
            npc<Chuckle>(" The crate was marked 'Dragon Plates'. Apparently they thought it was some kind of armour, when really it's just a plate with a dragon on it!")
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
    val code = stringEntry("Please enter your code.")
    val definition = codeDefinitions.get(code)
    if (definition.code.isBlank()) {
        player.message("Your code was not valid. Please check it and try again.")
        return@npcOperate
    }
    val success = player.inventory.transaction {
        for (item in definition.add) {
            add(item.id, item.amount)
        }
    }
    if (success) {
        player.message("Your code has been successfully processed.")
    }
}
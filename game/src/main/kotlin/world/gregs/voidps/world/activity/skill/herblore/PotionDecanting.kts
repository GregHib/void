package world.gregs.voidps.world.activity.skill.herblore

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

val logger = InlineLogger()

// prefix of potions that can be decanted.
val potions = setOf(
        "potion",
        "antipoison",
        "brew",
        "antidote"
)

npcOperate("Decant", "bob_barter_herbs") {
    if(decantPotions(player)) {
        npc<Cheerful>("There you go, chum.")
    } else {
        npc<Sad>("I wasn't able to decant your potions.")
    }
}
npcOperate("Talk-to", "bob_barter_herbs") {
    npc<Cheerful>("Hello, chum, fancy buyin' some designer jewellry? They've come all the way from Ardougne! Most pukka!")
    player<Talking>("Erm, no. I'm all set, thanks.")
    npc<Cheerful>("Okay, chum, would you like to show you the very latest potion prices?")
    choice {
        option<Talk>("Who are you?") {
            npc<Cheerful>("Why, I'm Bob! Your friendly seller of smashin' goods!")
            player<Talking>("So what do you have to sell?")
            npc<Talking>("Oh, not much at the moment. Cuz, ya know, business being so well and cushie.")
            player<Talking>("You don't really look like you're being so successful.")
            npc<Talking>("You plonka! It's all a show, innit! If I let people knows I'm in good business they'll want a share of the moolah!")
            player<Talking>("You conveniently have a response for everything.")
            npc<Talking>("That's the Ardougne way, my son.")
        }
        option<Talk>("Can you show me the prices for potions?") {
            npc<Sad>("The Grand Exchange is still under construction, hopefully it won't take long, then i'll have those prices for yah")
            // this should show an interface with price guide of herbs based off the grand exchange
        }
        option<Talk>("I'll leave you too it") { }
    }
}

// no G.E so this cannot be implemented yet.
npcOperate("Info-herbs", "bob_barter_herbs") {
    return@npcOperate

}

fun decantPotions(player: Player): Boolean {
    // Check if the player has any potions to decant
    if (player.inventory.items.none { item -> potions.any { potion -> item.id.contains(potion) } }) {
        return false
    }

    // Backup players inventory in case of an error we can restore the players items
    val originalItems = player.inventory.items.toList()

    try {
        val potionMap = mutableMapOf<String, Int>()

        // Remove potions and calculate total doses for each type
        player.inventory.items.filter { item -> potions.any { potion -> item.id.contains(potion) } }
                .toList().forEach { potion ->
            val baseId = potion.id.substringBeforeLast('_')
            potionMap[baseId] = potionMap.getOrDefault(baseId, 0) +
                    Integer.parseInt(potion.id.takeLast(1)) * potion.amount
            player.inventory.remove(potion.id)
        }

        // No Potion were decanted return false
        if (potionMap.isEmpty()) {
            return false
        }

        // Add decanted potions back into the players inventory
        potionMap.forEach { (baseId, totalDoses) ->
            var dosesLeft = totalDoses
            while (dosesLeft > 0) {
                val dose = if (dosesLeft >= 4) 4 else dosesLeft
                dosesLeft -= dose
                player.inventory.add("${baseId}_$dose", 1)
            }
        }

        // If the players inventory isn't changed return false
        if (originalItems.size == player.inventory.items.size && originalItems.containsAll(player.inventory.items.toList())) {
            return false
        }

        return true
    } catch (e: Exception) {
        logger.error { "Error while decanting potions for player ${player.name}" }
        // Restore the player's inventory in case of an error
        player.inventory.clear()
        player.inventory.transaction {
            for (item in originalItems) {
                player.inventory.add(item.id, item.amount)
            }
        }
        return false
    }
}
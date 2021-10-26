package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.purchase
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.Math.interpolate
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop

on<NPCOption>({ npc.def.name == "Bob" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        val choice = choice("""
            Give me a quest!
            I'd like to trade.
            Can you repair my items for me?
        """)
        when (choice) {
            1 -> npc("talk", "Sorry I don't have any quests for you at the moment.")
            2 -> {
                player("disregard", "I'd like to trade.")
                npc("cheerful", """
                    Great! I buy and sell pickaxes and hatchets. There are
                    plenty to choose from, and I've some free samples too.
                    Take your pick... or hatchet.
                """)
                player.events.emit(OpenShop("bobs_brilliant_axes"))
            }
            3 -> {
                player("upset", "Can you repair my items for me?")
                npc("think", """
                    Of course I can, though the material may cost you. Just
                    hand me the item and I'll have a look.
                """)
            }
        }
    }
}

on<InterfaceOnNPC>({ npc.def.name == "Bob" }) { player: Player ->
    player.talkWith(npc) {
        if (!repairable(item.id)) {
            npc("disregard", "Sorry friend, but I can't do anything with that.")
            return@talkWith
        }
        val cost = repairCost(player, item)
        npc("talk", "That'll cost you $cost gold coins to fix, are you sure?")
        val choice = choice("""
            Yes I'm sure!
            On second thoughts, no thanks.
        """)
        if (choice == 1 && player.purchase(cost)) {
            player.inventory.replace(item.id, repaired(item.id))
            npc("cheerful", "There you go. It's a pleasure doing business with you!")
        }
    }
}

fun repairable(item: String) = item.endsWith("_100") || item.endsWith("_75") || item.endsWith("_50") || item.endsWith("_25") || item.endsWith("_broken")

fun repaired(item: String) = item.replace("_100", "_new").replace("_75", "_new").replace("_50", "_new").replace("_25", "_new").replace("_broken", "_new")

fun repairCost(player: Player, item: Item): Int {
    val cost = item.def.cost
    val max = (cost * 0.6).toInt()
    val min = (cost * 0.4).toInt()
    return interpolate(player.levels.get(Skill.Smithing), max, min, 0, 99)
}
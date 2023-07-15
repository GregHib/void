package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation.interpolate
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.Upset
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

on<NPCOption>({ operate && target.id == "bob" && option == "Talk-to" }) { player: Player ->
    choice {
        option("Give me a quest!") {
            npc<Talk>("Sorry I don't have any quests for you at the moment.")
        }
        option<Unsure>("I'd like to trade.") {
            npc<Cheerful>("""
                Great! I buy and sell pickaxes and hatchets. There are
                plenty to choose from, and I've some free samples too.
                Take your pick... or hatchet.
            """)
            player.openShop("bobs_brilliant_axes")
        }
        option<Upset>("Can you repair my items for me?") {
            npc<Unsure>("""
                Of course I can, though the material may cost you. Just
                hand me the item and I'll have a look.
            """)
        }
    }
}

on<ItemOnNPC>({ operate && target.id == "bob" }) { player: Player ->
    if (!repairable(item.id)) {
        npc<Unsure>("Sorry friend, but I can't do anything with that.")
        return@on
    }
    val cost = repairCost(player, item)
    npc<Talk>("That'll cost you $cost gold coins to fix, are you sure?")
    choice {
        option("Yes I'm sure!") {
            val repaired = player.inventory.transaction {
                remove("coins", cost)
                replace(item.id, repaired(item.id))
            }
            if (repaired) {
                npc<Cheerful>("There you go. It's a pleasure doing business with you!")
            }
        }
        option("On second thoughts, no thanks.")
    }
}

fun repairable(item: String) = item.endsWith("_100") || item.endsWith("_75") || item.endsWith("_50") || item.endsWith("_25") || item.endsWith("_broken")

fun repaired(item: String) = item
    .replace("_100", "_new")
    .replace("_75", "_new")
    .replace("_50", "_new")
    .replace("_25", "_new")
    .replace("_broken", "_new")

fun repairCost(player: Player, item: Item): Int {
    val cost = item.def.cost
    val max = (cost * 0.6).toInt()
    val min = (cost * 0.4).toInt()
    return interpolate(player.levels.get(Skill.Smithing), max, min, 0, 99)
}
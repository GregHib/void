package content.area.misthalin.lumbridge

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation.interpolate
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Bob : Script {

    init {
        npcOperate("Talk-to", "bob") {
            choice {
                option("Give me a quest!") {
                    npc<Neutral>("Sorry I don't have any quests for you at the moment.")
                }
                option<Quiz>("I'd like to trade.") {
                    npc<Happy>("Great! I buy and sell pickaxes and hatchets. There are plenty to choose from, and I've some free samples too. Take your pick... or hatchet.")
                    openShop("bobs_brilliant_axes")
                }
                option<Sad>("Can you repair my items for me?") {
                    npc<Quiz>("Of course I can, though the material may cost you. Just hand me the item and I'll have a look.")
                }
            }
        }

        itemOnNPCOperate("*", "bob") { (_, item, slot) ->
            val repaired = Tables.itemOrNull("repairable_items.${item.id}.repaired")
            if (repaired == null) {
                npc<Quiz>("Sorry friend, but I can't do anything with that.")
                return@itemOnNPCOperate
            }
            val cost = repairCost(this, item)
            npc<Neutral>("That'll cost you $cost gold coins to fix, are you sure?")
            choice {
                option("Yes I'm sure!") {
                    val repaired = inventory.transaction {
                        remove("coins", cost)
                        clear(slot)
                        add(repaired)
                    }
                    if (repaired) {
                        npc<Happy>("There you go. It's a pleasure doing business with you!")
                    } else {
                        player<Sad>("Oh dear, I don't seem to have enough money.")
                    }
                }
                option("On second thoughts, no thanks.")
            }
        }
    }

    fun repairCost(player: Player, item: Item): Int {
        val cost = item.def.cost
        val max = (cost * 0.6).toInt()
        val min = (cost * 0.4).toInt()
        return interpolate(player.levels.get(Skill.Smithing), max, min, 0, 99)
    }
}

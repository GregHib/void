package content.area.misthalin.varrock.cooks_guild

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.type.random

class RomilyWeaklax : Script {
    init {
        npcOperate("Talk-to", "romily_weaklax") { (target) ->
            npc<Neutral>("Hello and welcome to my pie shop, how can I help you?")
            choice {
                option("I'd like to buy some pies.") {
                    openShop(target.def["shop", ""])
                }
                val pie = get("pie_task", "")
                if (pie != "") {
                    val amount = get("pie_task_amount", 0)
                    option<Neutral>("I've got those pies you wanted.") {
                        var removed = 0
                        inventory.transaction {
                            removed = removeToLimit(pie, amount)
                            add("coins", removed * reward(pie))
                        }
                        if (inventory.transaction.error != TransactionError.None || removed <= 0) {
                            npc<Sad>("Doesn't look like you have any of the $amount ${pie.toTitleCase()} I requested.")
                            return@option
                        }
                        val remaining = amount - removed
                        set("pie_task_amount", remaining)
                        if (remaining <= 0) {
                            npc<Happy>("Thank you very much!")
                        } else {
                            npc<Happy>("Thank you, if you could bring me the other $remaining that'd be great!")
                        }
                    }
                } else {
                    option<Quiz>("Do you need any help?") {
                        npc<Neutral>("Actually I could, you see I'm running out of stock and I don't have time to bake any more pies. Would you be willing to bake me some pies? I'll pay you well for them.")
                        choice {
                            option<Quiz>("Sure, what do you need?") {
                                val id = randomPie(levels.get(Skill.Cooking))
                                val amount = random.nextInt(4, 29)
                                set("pie_task", id)
                                set("pie_task_amount", amount)
                                npc<Happy>("Great, can you bake me $amount ${id.toTitleCase().plural(amount)} please.")
                            }
                            option<Sad>("Sorry, I can't help you.")
                        }
                    }
                }
                option<Neutral>("I'm good thanks.")
            }
        }
    }

    private fun reward(pie: String) = when (pie) {
        "meat_pie" -> 18
        "mud_pie" -> 67
        "apple_pie" -> 37
        "garden_pie" -> 30
        "fish_pie" -> 125
        "admiral_pie" -> 387
        "wild_pie" -> 227
        "summer_pie" -> 175
        else -> 15
    }

    private fun randomPie(level: Int): String {
        val pies = buildSet {
            if (level >= 10) {
                add("redberry_pie")
            }
            if (level >= 20) {
                add("meat_pie")
            }
            if (level >= 29) {
                add("mud_pie")
            }
            if (level >= 30) {
                add("apple_pie")
            }
            if (level >= 34) {
                add("garden_pie")
            }
            if (level >= 47) {
                add("fish_pie")
            }
            if (level >= 70) {
                add("admiral_pie")
            }
            if (level >= 85) {
                add("wild_pie")
            }
            if (level >= 95) {
                add("summer_pie")
            }
        }
        return pies.random(random)
    }
}
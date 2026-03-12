package content.skill.magic.book.modern

import content.entity.player.dialogue.type.choice
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.skill.magic.Magic
import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.queue

class Alchemy(val definitions: SpellDefinitions) : Script {
    init {
        onItem("modern_spellbook:*_level_alchemy") { item, id ->
            if (item.def.contains("destroy")) {
                message("This spell can not be cast on this item.")
                return@onItem
            }
            val spell = id.substringAfter(":")
            if (item.def.cost >= get("alchemy_warning_limit", Settings["magic.alchemy.warningLimit", 25_000])) {
                queue("alch_warning") {
                    choice("The item you are about to alch has a high value.") {
                        option("I wish to continue.") {
                            alch(player, spell, item)
                        }
                        option("I do not want to alch this item.")
                    }
                }
                return@onItem
            }

            alch(this, spell, item)
        }
    }

    private fun alch(player: Player, spell: String, item: Item) {
        player.tab(Tab.Inventory)
        val coins = (item.def.cost * if (spell == "high_level_alchemy") 0.6 else 0.4).toInt()
        if (player.hasClock("action_delay")) {
            return
        }
        player.inventory.transaction {
            removeItems(player, spell)
            remove(item.id)
            add("coins", coins)
        }
        when (player.inventory.transaction.error) {
            is TransactionError.Deficient -> return
            is TransactionError.Full -> player.inventoryFull("room in your inventory")
            TransactionError.Invalid -> return
            TransactionError.None -> {
                player.start("action_delay", 3)
                AuditLog.event(player, "alched", item, coins, spell == "high_level_alchemy", player.tile)
                val definition = definitions.get(spell)
                player.anim(Magic.animation(player, definition))
                player.gfx(Magic.graphic(player, definition))
                player.sound(spell)
                player.exp(Skill.Magic, definition.experience)
                player.tab(Tab.MagicSpellbook)
            }
        }
    }
}

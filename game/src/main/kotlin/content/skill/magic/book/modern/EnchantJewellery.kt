package content.skill.magic.book.modern

import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class EnchantJewellery : Script {
    init {
        onItem("modern_spellbook:enchant_level_*") { item, id ->
            if (hasClock("action_delay")) {
                return@onItem
            }
            val spell = id.substringAfter(":")
            val product = find(item)
            val type = Tables.string("jewellery_enchant.$spell.type")
            if (product == null) {
                message("This spell can only be cast on $type amulets, necklaces, rings and bracelets.")
                return@onItem
            }
            val level = Tables.int("jewellery_enchant.$spell.level")
            if (!has(Skill.Magic, level)) {
                return@onItem
            }
            start("action_delay", 1)
            inventory.transaction {
                removeItems(this@onItem, spell, message = false)
                replace(item.id, product)
            }
            when (inventory.transaction.error) {
                is TransactionError.Deficient -> {
                    // TODO proper messages - You do not have enough Cosmic Runes to cast this spell.
                    message("You do not have the required items to cast this spell.")
                    return@onItem
                }
                TransactionError.None -> {
                    if (item.id.endsWith("necklace") || item.id.endsWith("amulet")) {
                        gfx(spell.replace("_level_", "_jewellery_"))
                        anim(
                            when (spell) {
                                "enchant_level_1" -> "enchant_jewellery_1"
                                "enchant_level_2" -> "enchant_jewellery_2"
                                else -> "enchant_jewellery_3"
                            },
                        )
                        sound("enchant_${type}_amulet")
                    } else if (item.id.endsWith("ring")) {
                        gfx("enchant_ring")
                        anim("enchant_ring")
                        sound("enchant_${type}_ring")
                    } else {
                        gfx("enchant_ring")
                        sound("enchant_${type}_amulet")
                        anim(
                            when (spell) {
                                "enchant_level_1" -> "enchant_jewellery_1"
                                "enchant_level_2" -> "enchant_jewellery_2"
                                else -> "enchant_jewellery_3"
                            },
                        )
                    }
                    val xp = Tables.int("jewellery_enchant.$spell.xp") / 10.0
                    exp(Skill.Magic, xp)
                }
                else -> return@onItem
            }
        }
    }

    private fun find(item: Item): String? {
        val enchanted = Tables.getOrNull("jewellery_enchant") ?: return null
        for (id in enchanted.rows) {
            val row = Rows.get(id)
            val (ring, enchantedRing) = row.itemPair("ring")
            if (ring == item.id) {
                return enchantedRing
            }
            val (necklace, enchantedNecklace) = row.itemPair("necklace")
            if (necklace == item.id) {
                return enchantedNecklace
            }
            val (bracelet, enchantedBracelet) = row.itemPair("bracelet")
            if (bracelet == item.id) {
                return enchantedBracelet
            }
            val (amulet, enchantedAmulet) = row.itemPair("amulet")
            if (amulet == item.id) {
                return enchantedAmulet
            }
        }
        return null
    }
}

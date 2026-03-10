package content.skill.magic.book.modern

import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
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
            val enchanted = EnumDefinitions.stringOrNull(spell, item.id)
            val enchantLevel = spell.substringAfterLast("_").toInt()
            val type = EnumDefinitions.string("enchant_type", enchantLevel)
            if (enchanted == null) {
                message("This spell can only be cast on $type amulets, necklaces, rings and bracelets.")
                return@onItem
            }
            val level = EnumDefinitions.int("enchant_level", enchantLevel)
            if (!has(Skill.Magic, level)) {
                return@onItem
            }
            start("action_delay", 1)
            inventory.transaction {
                removeItems(this@onItem, spell, message = false)
                replace(item.id, enchanted)
            }
            when (inventory.transaction.error) {
                is TransactionError.Deficient -> {
                    // TODO proper messages - You do not have enough Cosmic Runes to cast this spell.
                    message("You do not have the required items to cast this spell.")
                    return@onItem
                }
                TransactionError.None -> {
                    if (item.id.endsWith("necklace") || item.id.endsWith("amulet")) {
                        gfx("enchant_jewellery_$enchantLevel")
                        anim(
                            when (enchantLevel) {
                                1 -> "enchant_jewellery_1"
                                2 -> "enchant_jewellery_2"
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
                            when (enchantLevel) {
                                1 -> "enchant_jewellery_1"
                                2 -> "enchant_jewellery_2"
                                else -> "enchant_jewellery_3"
                            },
                        )
                    }
                    val xp = EnumDefinitions.int("enchant_xp", enchantLevel) / 10.0
                    exp(Skill.Magic, xp)
                }
                else -> return@onItem
            }
        }
    }
}

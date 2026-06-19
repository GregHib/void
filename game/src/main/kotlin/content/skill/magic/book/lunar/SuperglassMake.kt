package content.skill.magic.book.lunar

import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.random

class SuperglassMake : Script {

    private val weeds = listOf("seaweed", "soda_ash", "swamp_weed")

    init {
        interfaceOption("Cast", "lunar_spellbook:superglass_make") {
            val spell = it.component
            if (hasClock("action_delay")) {
                return@interfaceOption
            }
            val sand = inventory.count("bucket_of_sand")
            val weed = weeds.sumOf { id -> inventory.count(id) }
            val pairs = minOf(sand, weed)
            if (pairs == 0) {
                message("You need a bucket of sand and seaweed or soda ash to cast this spell.")
                return@interfaceOption
            }
            if (!removeSpellItems(spell)) {
                return@interfaceOption
            }
            var glass = 0
            inventory.transaction {
                var remaining = pairs
                for (id in weeds) {
                    val count = minOf(remaining, inventory.count(id))
                    if (count == 0) {
                        continue
                    }
                    remove(id, count)
                    remaining -= count
                }
                remove("bucket_of_sand", pairs)
                repeat(pairs) {
                    glass += if (random.nextDouble() < 0.3) 2 else 1
                }
                add("molten_glass", glass)
            }
            if (inventory.transaction.error != TransactionError.None) {
                return@interfaceOption
            }
            start("action_delay", 2)
            anim("lunar_cast_charge")
            gfx(spell)
            sound(spell)
            exp(Skill.Magic, Tables.int("spells.$spell.xp") / 10.0)
            exp(Skill.Crafting, 10.0 * glass)
            message("You cast the spell and the items fuse into molten glass.")
        }
    }
}

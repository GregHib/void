package content.skill.magic.book.modern

import content.entity.gfx.areaGfx
import content.entity.player.inv.item.take.ItemTake
import content.entity.proj.shoot
import content.skill.magic.spell.removeSpellItems
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.Items
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.queue.queue

class TelekineticGrab : Script {
    init {
        onFloorItemApproach("modern_spellbook:telekinetic_grab") {
            approachRange(10)
            steps.clear()
            val spell = "telekinetic_grab"
            val floorItem = it.target
            face(floorItem.tile)
            val item = Items.takeable(this, floorItem.id) ?: return@onFloorItemApproach
            if (hasClock("action_delay")) {
                return@onFloorItemApproach
            }
            inventory.transaction.apply {
                start()
                add(item, floorItem.amount)
                revert()
            }
            when (inventory.transaction.error) {
                is TransactionError.Full -> return@onFloorItemApproach inventoryFull("to hold that item")
                is TransactionError.None -> if (!removeSpellItems(spell)) {
                    return@onFloorItemApproach
                }
                else -> return@onFloorItemApproach
            }
            anim("tele_grab_cast")
            gfx("tele_grab_cast")
            sound("tele_grab_cast")
            exp(Skill.Magic, Tables.int("spells.$spell.xp") / 10.0)

            val clientTicks = shoot("tele_grab_travel", floorItem.tile)
            areaSound("tele_grab_impact", floorItem.tile, delay = clientTicks, radius = 10)
            areaGfx("tele_grab_impact", floorItem.tile, delay = clientTicks)

            delay(3)
            queue("tele_grab", 3) {
                if (tile.level != floorItem.tile.level) {
                    message("Your telegrab fizzles as you move too far away.")
                    return@queue
                }
                if (!ItemTake.take(this, floorItem)) {
                    return@queue
                }
                start("action_delay", 3)
                AuditLog.event(this@onFloorItemApproach, "telegrab", floorItem, floorItem.tile)
                Items.take(this@onFloorItemApproach, floorItem)
            }
        }

        combatPrepare("magic") { target ->
            if (spell == "telekinetic_grab") {
                if (target is NPC) {
                    message("You can't use Telekinetic Grab on them.")
                }
                false
            } else {
                true
            }
        }
    }
}

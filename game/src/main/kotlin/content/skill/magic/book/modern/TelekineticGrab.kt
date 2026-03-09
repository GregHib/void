package content.skill.magic.book.modern

import content.entity.gfx.areaGfx
import content.entity.proj.shoot
import content.skill.magic.spell.SpellRunes.removeItems
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.Items
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.SetCharge.setCharge
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.CLIENT_TICKS

class TelekineticGrab(val definitions: SpellDefinitions) : Script {
    init {
        onFloorItemApproach("modern_spellbook:telekinetic_grab") {
            val spell = "telekinetic_grab"
            val floorItem = it.target
            val item = Items.takeable(this, floorItem.id) ?: return@onFloorItemApproach
            inventory.transaction.apply {
                removeItems(this@onFloorItemApproach, spell)
                add(item, floorItem.amount)
            }
            when (inventory.transaction.error) {
                is TransactionError.Full -> return@onFloorItemApproach inventoryFull("to hold that item")
                is TransactionError.Invalid -> inventory.transaction.revert()
                else -> return@onFloorItemApproach
            }
            if (!removeSpellItems(spell)) {
                return@onFloorItemApproach
            }
            val definition = definitions.get(spell)
            anim("tele_grab_cast")
            gfx("tele_grab_cast")
            sound("tele_grab_cast")
            exp(Skill.Magic, definition.experience)

            val clientTicks = shoot("tele_grab_travel", floorItem.tile)
            areaSound("tele_grab_impact", floorItem.tile, delay = clientTicks, radius = 10)
            areaGfx("tele_grab_impact", floorItem.tile, delay = clientTicks)

            softQueue("tele_grab", CLIENT_TICKS.toTicks(clientTicks)) {
                if (inventory.isFull() && (!inventory.stackable(item) || !inventory.contains(item))) {
                    inventoryFull()
                    return@softQueue
                }
                if (!FloorItems.remove(floorItem)) {
//                    message("Too late - it's gone!") TODO message?
                    return@softQueue
                }
                inventory.transaction {
                    val index = add(item, floorItem.amount)
                    if (floorItem.charges > 0 && index != -1) {
                        setCharge(index, floorItem.charges)
                    }
                }
                when (inventory.transaction.error) {
                    TransactionError.None -> {
                        AuditLog.event(this@onFloorItemApproach, "telegrab", floorItem, floorItem.tile)
                        sound("take_item")
                    }
                    is TransactionError.Full -> inventoryFull()
                    else -> {}
                }
            }
        }
    }
}

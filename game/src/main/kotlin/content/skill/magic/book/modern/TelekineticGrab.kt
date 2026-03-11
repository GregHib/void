package content.skill.magic.book.modern

import content.entity.gfx.areaGfx
import content.entity.player.inv.item.take.ItemTake
import content.entity.proj.shoot
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.Items
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.CLIENT_TICKS

class TelekineticGrab(val definitions: SpellDefinitions) : Script {
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
            val definition = definitions.get(spell)
            anim("tele_grab_cast")
            gfx("tele_grab_cast")
            sound("tele_grab_cast")
            exp(Skill.Magic, definition.experience)

            val clientTicks = shoot("tele_grab_travel", floorItem.tile)
            areaSound("tele_grab_impact", floorItem.tile, delay = clientTicks, radius = 10)
            areaGfx("tele_grab_impact", floorItem.tile, delay = clientTicks)

            softQueue("tele_grab", CLIENT_TICKS.toTicks(clientTicks) + 1) {
                if (player.tile.level != floorItem.tile.level) {
                    message("Your telegrab fizzles as you move too far away.")
                    return@softQueue
                }
                if (!ItemTake.take(player, floorItem)) {
                    return@softQueue
                }
                start("action_delay", 3)
                AuditLog.event(this@onFloorItemApproach, "telegrab", floorItem, floorItem.tile)
                if (tile != floorItem.tile) {
                    face(floorItem.tile.delta(tile))
                    anim("take")
                }
                Items.take(this@onFloorItemApproach, floorItem)
            }
        }

        onNPCApproach("modern_spellbook:telekinetic_grab") {
            message("You can't use Telekineetic Grab on them.")
        }
    }
}

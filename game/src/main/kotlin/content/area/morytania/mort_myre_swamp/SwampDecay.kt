package content.area.morytania.mort_myre_swamp

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.any
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random
import kotlin.random.nextInt

class SwampDecay : Script {
    val immunity = listOf(
        Item("silver_sickle_b"),
        Item("silver_sickle_emerald_b"),
        Item("rod_of_ivandis"),
        Item("rod_of_ivandis_2"),
        Item("rod_of_ivandis_3"),
        Item("rod_of_ivandis_4"),
        Item("rod_of_ivandis_5"),
        Item("rod_of_ivandis_6"),
        Item("rod_of_ivandis_7"),
        Item("rod_of_ivandis_8"),
        Item("rod_of_ivandis_9"),
        Item("rod_of_ivandis_10"),
    )

    init {
        entered("mort_myre_swamp") {
            softTimers.start("swamp_decay")
        }

        timerStart("swamp_decay") {
            230 // 2 minutes 18 seconds
        }

        timerTick("swamp_decay") {
            if (inventory.any(immunity) || equipment.any(immunity)) {
                return@timerTick Timer.CONTINUE
            }
            if (tile in Areas["mort_myre_swamp"]) {
                message("The swamp decays you!")
                directHit(random.nextInt(10..30))
                gfx("swamp_decay")
                Timer.CONTINUE
            } else {
                message("The swamp decay effect is now over.")
                Timer.CANCEL
            }
        }
    }
}

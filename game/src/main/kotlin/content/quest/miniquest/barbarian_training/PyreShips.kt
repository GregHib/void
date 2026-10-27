package content.quest.miniquest.barbarian_training

import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.skill.woodcutting.Hatchet
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Burning barbarian remains on a pyre ship by Otto's lake.
 * Chewed bones lay a hero to rest, blessing future bone burying; mangled bones release a ferocious spirit.
 */
class PyreShips(val drops: DropTables) : Script {

    data class PyreLog(val level: Int, val crafting: Double, val firemaking: Double, val bones: Int)

    val logs = mapOf(
        "magic_logs" to PyreLog(85, 75.9, 303.8, 5),
        "yew_logs" to PyreLog(70, 50.6, 202.5, 4),
        "mahogany_logs" to PyreLog(60, 39.3, 157.5, 4),
        "maple_logs" to PyreLog(55, 33.7, 135.0, 3),
        "arctic_pine_logs" to PyreLog(52, 31.2, 125.0, 3),
        "teak_logs" to PyreLog(45, 26.2, 105.0, 3),
        "willow_logs" to PyreLog(40, 22.5, 90.0, 2),
        "oak_logs" to PyreLog(25, 15.0, 60.0, 2),
        "achey_tree_logs" to PyreLog(11, 10.0, 40.0, 1),
        "logs" to PyreLog(11, 10.0, 40.0, 1),
    )

    val carving = mapOf(
        "inferno_adze" to "carve_pyre_inferno_adze",
        "volatile_clay_hatchet" to "carve_pyre_volatile_clay",
        "sacred_clay_hatchet" to "carve_pyre_sacred_clay",
        "dragon_hatchet" to "carve_pyre_dragon",
        "rune_hatchet" to "carve_pyre_rune",
        "adamant_hatchet" to "carve_pyre_adamant",
        "mithril_hatchet" to "carve_pyre_mithril",
        "black_hatchet" to "carve_pyre_black",
        "steel_hatchet" to "carve_pyre_steel",
        "iron_hatchet" to "carve_pyre_iron",
        "bronze_hatchet" to "carve_pyre_bronze",
    )

    init {
        objectOperate("Construct", "pyre_site") { (target) ->
            if (!barbarianTrainingStarted("pyre")) {
                statement(BarbarianTraining.NOT_STARTED_MESSAGE)
                return@objectOperate
            }
            val spirit = NPCs.findOrNull(tile.regionLevel) { it.id == "ferocious_barbarian_spirit" && it["owner", ""] == accountName }
            if (spirit != null) {
                // TODO confirm message (from 2009scape)
                message("You must defeat the barbarian spirit first.")
                return@objectOperate
            }
            val bones = listOf("chewed_bones", "mangled_bones").firstOrNull { inventory.contains(it) }
            if (bones == null) {
                // TODO confirm message (from 2009scape)
                message("You need chewed bones or mangled bones in order to do this.")
                return@objectOperate
            }
            val hatchet = Hatchet.best(this)
            if (hatchet == null) {
                // TODO confirm message
                message("You need a hatchet in order to do this.")
                return@objectOperate
            }
            if (!inventory.contains("tinderbox") && !BOWS.any { carriesItem(it) }) {
                // TODO confirm message
                message("You need a tinderbox or a bow in order to do this.")
                return@objectOperate
            }
            val held = logs.entries.filter { inventory.contains(it.key) }
            if (held.isEmpty()) {
                message("You don't have any logs.")
                return@objectOperate
            }
            val log = held.firstOrNull { (_, log) -> levels.get(Skill.Firemaking) >= log.level && levels.get(Skill.Crafting) >= log.level }
            if (log == null) {
                val lowest = held.last().value.level
                has(Skill.Firemaking, lowest, true) && has(Skill.Crafting, lowest, true)
                return@objectOperate
            }
            val (logId, pyre) = log
            face(target)
            carving[hatchet.id]?.let { anim(it) }
            delay(4)
            inventory.transaction {
                remove(logId)
                remove(bones)
            }
            if (inventory.transaction.failed) {
                return@objectOperate
            }
            exp(Skill.Crafting, pyre.crafting)
            exp(Skill.Firemaking, pyre.firemaking)
            if (bones == "chewed_bones") {
                // TODO confirm how long the peaceful spirit stays
                val npc = NPCs.add("peaceful_barbarian_spirit", target.tile, ticks = 5)
                npc.face(this)
                set("barbarian_pyre_bones", (get("barbarian_pyre_bones", 0) + pyre.bones).coerceAtMost(MAX_BLESSED_BONES))
                // TODO confirm message (from 2009scape)
                message("The ancient barbarian is laid to rest. Your future prayer training is blessed, as his spirit ascends to a glorious afterlife. Spirits drop an object into your pack.")
                val reward = drops.get("peaceful_barbarian_spirit_drop_table")?.roll()?.firstOrNull()?.toItem()
                if (reward != null) {
                    addOrDrop(reward.id, reward.amount)
                }
                barbarianTaskDone("pyre")
            } else {
                val npc = NPCs.add("ferocious_barbarian_spirit", target.tile, ticks = TimeUnit.MINUTES.toTicks(5), owner = this)
                npc.interactPlayer(this, "Attack")
            }
            // Replacing the site ends this interaction so it must come last
            // TODO confirm the pyre object stages (carved log, boat, bones, lit) and their timings
            target.replace("pyre_boat_lit", ticks = 10)
        }
    }

    companion object {
        const val MAX_BLESSED_BONES = 200
        val BOWS = listOf("shortbow", "longbow", "oak_shortbow", "oak_longbow", "willow_shortbow", "willow_longbow", "maple_shortbow", "maple_longbow", "yew_shortbow", "yew_longbow", "magic_shortbow", "magic_longbow", "training_bow", "seercull", "sacred_clay_bow")
    }
}

package content.quest.miniquest.barbarian_training

import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.skill.woodcutting.Hatchet
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
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

    init {
        objectOperate("Construct", "pyre_site") { (target) ->
            if (!barbarianTrainingStarted("pyre")) {
                statement(BarbarianTraining.NOT_STARTED_MESSAGE)
                return@objectOperate
            }
            val spirit = NPCs.findOrNull(tile.regionLevel) { it.id == "ferocious_barbarian_spirit" && it["owner", ""] == accountName }
            if (spirit != null) {
                // TODO proper message
                message("You must defeat the barbarian spirit first.")
                return@objectOperate
            }
            val bones = listOf("chewed_bones", "mangled_bones").firstOrNull { inventory.contains(it) }
            if (bones == null) {
                // TODO proper message
                message("You need chewed bones or mangled bones in order to do this.")
                return@objectOperate
            }
            val hatchet = Hatchet.best(this)
            if (hatchet == null) {
                // TODO proper message
                message("You need a hatchet in order to do this.")
                return@objectOperate
            }
            if (!inventory.contains("tinderbox") && !BOWS.any { carriesItem(it) }) {
                // TODO proper message
                message("You need a tinderbox or a bow in order to do this.")
                return@objectOperate
            }
            val held = Tables.get("pyre_logs").rows().filter { inventory.contains(it.rowId) }
            if (held.isEmpty()) {
                message("You don't have any logs.")
                return@objectOperate
            }
            val log = held.firstOrNull { levels.get(Skill.Firemaking) >= it.int("level") && levels.get(Skill.Crafting) >= it.int("level") }
            if (log == null) {
                val lowest = held.last().int("level")
                has(Skill.Firemaking, lowest, true) && has(Skill.Crafting, lowest, true)
                return@objectOperate
            }
            face(target)
            anim("carve_pyre_${hatchet.id.removeSuffix("_hatchet")}")
            delay(4)
            inventory.transaction {
                remove(log.rowId)
                remove(bones)
            }
            if (inventory.transaction.failed) {
                return@objectOperate
            }
            exp(Skill.Crafting, log.int("crafting_xp") / 10.0)
            exp(Skill.Firemaking, log.int("firemaking_xp") / 10.0)
            if (bones == "chewed_bones") {
                // TODO confirm how long the peaceful spirit stays
                val npc = NPCs.add("peaceful_barbarian_spirit", target.tile, ticks = 5)
                npc.face(this)
                set("barbarian_pyre_bones", (get("barbarian_pyre_bones", 0) + log.int("bones")).coerceAtMost(MAX_BLESSED_BONES))
                // TODO proper message
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

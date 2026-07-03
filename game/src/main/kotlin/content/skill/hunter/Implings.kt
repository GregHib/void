package content.skill.hunter

import content.entity.player.dialogue.type.item
import content.entity.player.inv.item.addOrDrop
import content.quest.questCompleted
import content.skill.melee.weapon.weapon
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import kotlin.random.nextInt

class Implings(val dropTables: DropTables) : Script {
    init {
        worldSpawn {
            respawnImplings()
        }

        settingsReload {
            respawnImplings()
        }

        npcOperate("Catch", "*_impling") { (target) ->
            val row = Rows.getOrNull("implings.${target.id}") ?: return@npcOperate
            val net = weapon.id == "butterfly_net" || weapon.id == "magic_butterfly_net"
            val level = if (net) row.int("level") else row.int("level") + 10
            if (!has(Skill.Hunter, level, message = if (net) "to catch this impling" else "to catch this impling barehanded")) {
                return@npcOperate
            }
            val puroPuro = tile in Areas["puro_puro"]
            if (target.id.startsWith("pirate_impling") && !questCompleted("rocking_out")) {
                message("You need to have completed Rocking Out to catch this impling.")
                return@npcOperate
            }
            val hasJar = inventory.contains("impling_jar")
            if (puroPuro && !hasJar) {
                message("You do not have an empty impling jar in which to keep an impling.")
                return@npcOperate
            }
            if (!puroPuro && !hasJar && inventory.spaces < 2) {
                message("You'll need to clear some space in your pack to catch this impling barehanded.")
                return@npcOperate
            }
            anim(if (net) "butterfly_catch" else "catch_impling_barehanded")
            target.anim(if (net) "impling_caught" else "caught_impling_barehanded")
            delay(2)
            var chance = row.intRange("chance")
            if (weapon.id != "butterfly_net") { // Barehanded or magic net
                chance = (chance.first + 20.. chance.last + 20)
            }
            if (!Level.success(levels.get(Skill.Hunter), chance)) {
                target.mode = Retreat(target, this)
                // TODO proper message
                return@npcOperate
            }
            target.hide = true
            target.softTimers.start("reveal_imp")
            if (target.id == "spirit_impling" && random.nextInt(3) != 0) {
                // https://youtu.be/O5_IjnlYXrU?&t=129
                val tables = dropTables.getValue("spirit_impling_charms")
                val item = tables.roll(player = this).single().toItem()
                addOrDrop(item.id, item.amount)
                message("You manage to catch the impling. It drops charms and flies away.")
                item(item.id, "The impling was carrying ${if (item.amount == 1) "a" else "some"} ${item.id.toLowerSpaceCase().plural(item.amount)}.")
                return@npcOperate
            }
            if (!puroPuro && !hasJar) {
                val tables = dropTables.getValue(dropTable(this, target.id))
                val drops = tables.roll(player = this)
                for (drop in drops) {
                    val item = drop.toItem()
                    addOrDrop(item.id, item.amount)
                }
                message("You manage to catch the impling and acquire some loot.", ChatType.Filter)
            } else {
                inventory.remove("impling_jar")
                inventory.add(row.item("jar"))
                message("You manage to catch the impling and squeeze it into a jar.", ChatType.Filter)
            }
            if (puroPuro) {
                exp(Skill.Hunter, row.int("xp_puro") / 10.0)
            } else {
                exp(Skill.Hunter, row.int("xp") / 10.0)
            }
        }

        npcTimerStart("reveal_impling") { TimeUnit.MINUTES.toTicks(Settings["hunter.implings.revealMinutes", 2]) }

        npcTimerTick("reveal_impling") { Timer.CANCEL }

        npcTimerStop("reveal_impling") {
            hide = false
        }

        // https://youtu.be/O5_IjnlYXrU?&t=165
        // The imp tried to steal one of your implings, but you stopped him!
        // You use your strength to push through the wheat in the most efficient fashion.
        // You use your strength to push through the wheat.
        // You push through the wheat. It's hard work, though.
    }

    private val active = mutableListOf<NPC>()

    private fun respawnImplings() {
        if (!World.members) {
            return
        }
        World.queue("impling_spawning", TimeUnit.MINUTES.toTicks(Settings["hunter.implings.respawnCycleMinutes", 30])) {
            for (npc in active) {
                NPCs.remove(npc)
            }
            active.clear()
            spawnImplings(4, "impling_high_tier", "impling_spawns")
            val count = random.nextInt(5..10)
            spawnImplings(count, "impling_mid_tier", "impling_spawns")
            spawnImplings(count * 2, "impling_low_tier", "impling_spawns")
            spawnImplings(1, "impling_low_tier", "impling_always_spawns")
        }
    }

    private fun spawnImplings(count: Int, table: String, spawns: String) {
        val high = dropTables.getValue(table)
        val lowTier = table.endsWith("low_tier")
        for (drop in (0 until count).flatMap { high.roll() }) {
            var rows = Tables.get(spawns).rows()
            if (!lowTier) {
                rows = rows.filter { !it.bool("low_tier") }
            }
            val row = rows.random(random)
            val tile = row.tile("tile")
            spawnImpling(drop.id.removeSuffix("_jar"), tile)
        }
    }

    private fun spawnImpling(id: String, tile: Tile) {
        val npc = NPCs.add(id, tile)
        npc.hide = true
        npc.softTimers.start("reveal_impling")
        active.add(npc)
    }

    companion object {
        fun dropTable(player: Player, id: String): String {
            if (id.startsWith("pirate_impling") && player.tile in Areas["trouble_brewing"]) {
                return "${id}_trouble_brewing_drop_table"
            }
            return "${id}_drop_table"
        }
    }
}

package content.area.kharidian_desert.pollnivneach.dungeon

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.quest.clearInstance
import content.quest.instanceOffset
import content.quest.setInstanceLogout
import content.quest.smallInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

class PollnivneachDungeon : Script {
    init {
        objectOperate("Pass", "pollnivneach_dungeon_barrier_north") { (target) ->
            val offset = instanceOffset()
            val boss = boss(target.tile.minus(offset), "tile") ?: return@objectOperate
            if (!get("killed_${boss}", false)) {
                statement("This portal leads to the lair of a ferocious creature. Are you sure you want to do battle?")
                choice("Do you want to head into the fray?") {
                    option("Yes, I feel brave.") {
                        val row = Rows.get("desert_dungeon_boss.${boss}")
                        smallInstance(Region(row.int("region")))
                        setInstanceLogout(row.tile("exit"))
                        delay(3)
                        val offset = instanceOffset()
                        tele(offset.tile(row.tile("player_spawn")))
                        val boss = NPCs.add(boss, offset.tile(row.tile("npc_spawn")))
                        message("Your surroundings shift to become familiar, as you face a powerful ${row.string("type")}.")
                        boss.interactPlayer(this, "Attack")
                    }
                    option("No way!")
                }
                return@objectOperate
            }
            face(target)
            val x = tile.x.coerceIn(target.tile.x, target.tile.x + 2)
            val y = if (tile.y <= target.tile.y) target.tile.y + 1 else target.tile.y - 1
            walkOverDelay(tile.copy(x = x))
            anim("pass_through_barrier")
            gfx("pass_barrier_red")
            exactMoveDelay(Tile(x, y), delay = 30)
        }

        exited("pollnivneach_dungeon_boss") {
            clearInstance()
        }

        objectOperate("Pass", "pollnivneach_dungeon_barrier") { (target) ->
            val offset = instanceOffset()
            val boss = boss(target.tile.minus(offset), "inside")
            if (boss != null) {
                clearInstance()
                tele(Tables.tile("desert_dungeon_boss.$boss.exit"))
                return@objectOperate
            }
            face(target)
            val x = if (tile.x <= target.tile.x) target.tile.x + 1 else target.tile.x - 1
            val y = tile.y.coerceIn(target.tile.y, target.tile.y + 2)
            walkOverDelay(tile.copy(y = y))
            message("You pass through the mystic barrier, which feels odd.")
            anim("pass_through_barrier")
            gfx("pass_barrier_red")
            exactMoveDelay(Tile(x, y), delay = 30)
        }

        objTeleportLand("Climb-down", "pollnivneach_well") { _, _ ->
            message("You descend into the somewhat smoky depths of the well, to the accompaniment of eery wails.")
            // TODO quest req, smoke interface + damage
        }

    }

    private fun boss(tile: Tile, key: String): String? {
        for (row in Tables.get("dessert_dungeon_barriers").rows()) {
            if (row.tile(key) == tile) {
                return row.npc("boss")
            }
        }
        return null
    }
}
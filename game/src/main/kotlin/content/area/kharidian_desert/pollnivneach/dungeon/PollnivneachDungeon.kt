package content.area.kharidian_desert.pollnivneach.dungeon

import content.entity.combat.killer
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.quest.instanceOffset
import content.quest.setInstanceLogout
import content.quest.smallInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

class PollnivneachDungeon : Script {
    init {
        // TODO barrier gfx
        objectOperate("Pass", "pollnivneach_dungeon_barrier") { (target) ->
            statement("This portal leads to the lair of a ferocious creature. Are you sure you want to do battle?")
            choice("Do you want to head into the fray?") {
                option("Yes, I feel brave.") {
                    smallInstance(Region(12356))
//                    setInstanceLogout()
                    val offset = instanceOffset()

                    tele(offset.add(3127, 4373))
                    val boss = NPCs.add("monsterous_cave_crawler", Tile(3121, 4384))

                    message("Your surroundings shift to become familiar, as you face a powerful turoth.")
//                    tele(offset.add(3089, 4318))
//                    val boss = NPCs.add("mightiest_turoth", Tile(3088, 4324))

                    message("Your surroundings shift to become familiar, as you face a powerful basilisk.")
//                    tele(offset.add(3123, 4333))
//                    val boss = NPCs.add("basalisk_boss", Tile(3115, 4322))

                    message("Your surroundings shift to become familiar, as you face a powerful kurask.")
//                    tele(offset.add(3089, 4322))
//                    val boss = NPCs.add("kurask_overlord", Tile(3091, 4323))

                    // 1447

                }
                option("No way!")
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

        npcDespawn("monsterous_cave_crawler") {
            val killer = killer
            softQueue("shift_back") {
//                statment("You shift back to reality, having defeated this boss. You may now pass this barrier freely.")
            }
        }

        objectOperate("Pass", "pollnivneach_dungeon_barrier_north") { (target) ->
            face(target)
            val x = tile.x.coerceIn(target.tile.x, target.tile.x + 2)
            val y = if (tile.y <= target.tile.y) target.tile.y + 1 else target.tile.y - 1
            walkOverDelay(tile.copy(x = x))
            anim("pass_through_barrier")
            gfx("pass_barrier_red")
            exactMoveDelay(Tile(x, y), delay = 30)
        }

        objTeleportLand("Climb-down", "pollnivneach_well") { _, _ ->
            message("You descend into the somewhat smoky depths of the well, to the accompaniment of eery wails.")
        }

    }
}
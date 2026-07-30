package content.minigame.mage_arena

import content.area.wilderness.leverTeleportBlocked
import content.entity.gfx.areaGfx
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class MageArenaChamber : Script {

    val capes = setOf("saradomin_cape", "guthix_cape", "zamorak_cape")

    init {
        objectOperate("Pull", "mage_arena_lever_in") {
            if (get("mage_arena", "unstarted") !in setOf("completed", "staff_received")) {
                npc<Neutral>("kolodion", "You're not allowed in there. Come downstairs if you want to enter my arena.")
                return@objectOperate
            }
            if (leverTeleportBlocked()) {
                return@objectOperate
            }
            pullLever()
            tele(Tile(3105, 3951))
            land()
        }

        objectOperate("Pull", "mage_arena_lever_out") {
            if (leverTeleportBlocked()) {
                return@objectOperate
            }
            pullLever()
            tele(Tile(3105, 3956))
            land()
        }

        objectOperate("Step-into", "sparkling_pool_mage_bank") { (target) ->
            if (get("mage_arena", "unstarted") != "completed" && get("mage_arena", "unstarted") != "staff_received") {
                walkToDelay(Tile(2542, 4718))
                message("You step into the pool.")
                walkOverDelay(Tile(2542, 4719))
                message("Your boots get wet.")
                tele(Tile(2542, 4718))
                return@objectOperate
            }
            jumpIn(start = Tile(2542, 4718), pool = Tile(2542, 4720), destination = Tile(2509, 4689))
        }

        objectOperate("Step-into", "sparkling_pool_chamber") {
            jumpIn(start = Tile(2509, 4689), pool = Tile(2509, 4687), destination = Tile(2542, 4718))
        }

        objectOperate("Pray at", "statue_of_saradomin,statue_of_zamorak,statue_of_guthix") { (target) ->
            val god = target.id.removePrefix("statue_of_")
            walkToDelay(target.tile.addY(-2), forceWalk = true)
            face(target.tile)
            anim("human_pray")
            message("You kneel and begin to chant to ${god.toSentenceCase()}...")
            delay(3)
            if (capes.any { ownsItem(it) }) {
                message("...but there is no response.")
                return@objectOperate
            }
            val drop = target.tile.addY(-1)
            FloorItems.add(drop, "${god}_cape", owner = this)
            areaGfx("cape_appear", drop)
            message("You feel a rush of energy charge through your veins. Suddenly a cape appears before you.")
        }

        takeable("saradomin_cape,guthix_cape,zamorak_cape") { item, _ ->
            if (capes.any { it != item.id && ownsItem(it) }) {
                message("You may only possess one sacred cape at a time. The conflicting powers of the capes drive them apart.")
                null
            } else {
                item.id
            }
        }

        itemOption("Drop", "saradomin_cape,guthix_cape,zamorak_cape") { (item, slot) ->
            if (inventory.remove(slot, item.id, item.amount)) {
                message(
                    when (item.id) {
                        "saradomin_cape" -> "The cape disappears in a flash of light as it touches the ground."
                        "guthix_cape" -> "The cape disintegrates as it touches the earth."
                        else -> "The cape ignites and burns up as it touches the ground."
                    },
                )
            }
        }
    }

    suspend fun Player.pullLever() {
        message("You pull the lever...", ChatType.Filter)
        anim("pull_lever")
        start("movement_delay", 3)
        delay(2)
        anim("teleport_modern")
        sound("teleport")
        gfx("teleport_modern")
        delay(3)
    }

    fun Player.land() {
        sound("teleport_land")
        gfx("teleport_land_modern")
        anim("teleport_land_modern")
    }

    suspend fun Player.jumpIn(start: Tile, pool: Tile, destination: Tile) {
        message("You step into the pool of sparkling water. You feel the energy rush through your veins.")
        walkToDelay(start, forceWalk = true)
        delay(1)
        face(pool)
        delay(1)
        sound("sparkling_pool_step")
        val distance = tile.distanceTo(pool)
        anim("sparkling_pool_jump")
        exactMove(pool, delay = distance * 30)
        delay(distance - 1)
        gfx("big_splash")
        sound("water_splash")
        delay(2)
        clearAnim()
        tele(destination)
    }
}

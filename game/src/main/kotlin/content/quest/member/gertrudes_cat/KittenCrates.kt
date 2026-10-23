package content.quest.member.gertrudes_cat

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class KittenCrates : Script {
    init {
        objectOperate("Search", "crate_17") { interact ->
            val player = interact.player
            val crate = interact.target

            if (interact.target.tile !in Areas["kitten_search_area"]) {
                return@objectOperate
            }
            if (get("three_little_kittens_found", false)) {
                message("You search the crate.")
                pause(1)
                message("You find nothing.")
                return@objectOperate
            }

            val fluffs = NPCs.findBySpawn(Tile(3306, 3512, 1), "fluffs")
            val kittenCoords = fluffs["kittens_location", -1]
            var kittenCrate = Tile(kittenCoords)
            if (crate.tile == kittenCrate) {
                if (player.inventory.isFull()) {
                    message("<red>You find three little kittens but you have no space for them in your backpack.") // TODO proper message
                    return@objectOperate
                }
                set("three_little_kittens_found", true)
                player.inventory.add("three_little_kittens")
                statement("You find three little kittens! You carefully place them in your backpack. This explains why Fluffs is so agitated.")
            } else {
                message("You search the crate.")
                pause(1)
                message("You find nothing.")
            }
        }

        npcSpawn("fluffs") {
            if (tile.level == 1) {
                softTimers.start("fluffs_kittens")
            }
        }

        npcTimerStart("fluffs_kittens") { 25 } // This is 6 ticks on osrs which seems too fast

        npcTimerTick("fluffs_kittens") {
            val tile = Tables.get("lumber_crates").rows().random(random).tile("tile")
            set("kittens_location", tile.id)
            areaSound("kittens_mew", tile, radius = 10)
            // TODO crate animation
            Timer.CONTINUE
        }
    }
}

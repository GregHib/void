package content.quest.member.gertrudes_cat

import content.area.misthalin.varrock.KITTENS_HIDING_SPOT
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

const val KITTENS_FOUND = "three_little_kittens_found"

class KittenCrates : Script {
    init {
        objectOperate("Search", "crate_17") { interact ->
            val player = interact.player
            val crate = interact.target

            if(get(KITTENS_FOUND, false)) {
                return@objectOperate
            }
            if(interact.target.tile !in Areas["kitten_search_area"]) {
                return@objectOperate
            }

            val kittenCoords = get(KITTENS_HIDING_SPOT, -1)
            var kittenCrate = Tile(kittenCoords)
            if(crate.tile == kittenCrate){
                if(player.inventory.isFull()){
                    message("<red>You find three little kittens but you have no space for them in your backpack.")
                    return@objectOperate
                }
                set(KITTENS_FOUND, true)
                player.inventory.add("three_little_kittens")
                statement("You find three little kittens! You carefully place them in your backpack. This explains why Fluffs is so agitated.")
            }
        }
    }
}
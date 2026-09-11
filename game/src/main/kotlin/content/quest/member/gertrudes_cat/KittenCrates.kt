package content.quest.member.gertrudes_cat

import content.area.misthalin.varrock.KITTENS_HIDING_SPOT
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.type.Tile

var KITTEN_CRATES = mutableSetOf<Tile>()

class KittenCrates : Script {

    init {
        objectSpawn("Crate") {
            if(this.tile in Areas["kitten_search_area"]){
                KITTEN_CRATES += tile
            }
        }
        objectOperate("Search", "Crate") { interact ->
            val player = interact.player
            val crate = interact.target
            if(interact.target.tile !in Areas["kitten_search_area"]) {
                return@objectOperate
            }
            var kittenCrate = get(KITTENS_HIDING_SPOT, Tile(0, 0))
            if(crate.tile == kittenCrate){
                // Cat found!
            }
        }
    }
}
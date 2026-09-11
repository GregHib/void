package content.quest.member.gertrudes_cat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas

class KittenCrates : Script {
    init {
        objectSpawn("Crate") {
            if(this.tile in Areas["kitten_search_area"]){

            }
        }
        objectOperate("Search", "Crate") { interact ->
            if(interact.target.tile in Areas["kitten_search_area"]) {

            }
        }
    }
}
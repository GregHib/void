package content.area.wilderness.bandit_camp

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class BanditCampRefusals : Script {

    init {
        npcOperate("Talk-to", "black_heather,donny_the_lad,speedy_keith") { (target) ->
            message("${target.def.name} is not interested in talking.")
        }
    }
}

package content.skill.farming

import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.Operate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Script

@Script
class FarmingPatch(val floorItems: FloorItems) : Api {

    @Operate("Rake", "veg_patch_weeds_#")
    override suspend fun operate(player: Player, target: GameObject, option: String) {
        val current = player["allotment_falador_se", "weeds_super"]
        val next = when (current) {
            "weeds_super" -> "weeds_2"
            "weeds_compost" -> "weeds_1"
            "weeds_none" -> "weeds_0"
            "weeds_2" -> "weeds_1"
            "weeds_1" -> "weeds_0"
            else -> return
        }
        player["allotment_falador_se"] = next
        player.addOrDrop("weeds")
        player.timers.start("farming_tick")
    }

}
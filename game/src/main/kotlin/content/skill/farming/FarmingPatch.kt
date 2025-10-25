package content.skill.farming

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.Operate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script

@Script
class FarmingPatch : Api {
    @Operate("Rake", "farming_veg_patch_#")
    override suspend fun operate(player: Player, target: GameObject, option: String) {
        println("Operate")
    }

}
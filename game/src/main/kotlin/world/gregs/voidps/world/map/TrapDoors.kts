package world.gregs.voidps.world.map

import world.gregs.voidps.engine.entity.character.mode.interact.onOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.utility.toTicks
import java.util.concurrent.TimeUnit

onOperate({ target.id.startsWith("trapdoor_") && target.id.endsWith("_closed")}) { player: Player, obj: GameObject ->
    player.setAnimation("open_chest")
    obj.replace(obj.id.replace("_closed", "_opened"), ticks = TimeUnit.MINUTES.toTicks(3))
}
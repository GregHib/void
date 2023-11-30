package world.gregs.voidps.world.map.wilderness

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.weapon
import java.util.concurrent.TimeUnit

on<ObjectOption>({ operate && def.stringId.startsWith("web") && option == "Slash" }, Priority.HIGH) { player: Player ->
    arriveDelay()
    if (player.weapon.def["slash_attack", 0] <= 0) {
        player.message("Only a sharp blade can cut through this sticky web.")
        cancel()
        return@on
    }
    slash(player, target)
}

on<ItemOnObject>({ operate && target.id.startsWith("web") }) { player: Player ->
    if (item.id == "knife" || item.def["slash_attack", 0] > 0) {
        player.message("Only a sharp blade can cut through this sticky web.")
        cancel()
        return@on
    }
    slash(player, target)
}

fun slash(player: Player, target: GameObject) {
    player.setAnimation("dagger_slash")
    target.replace("web_slashed", ticks = TimeUnit.MINUTES.toTicks(1))
}
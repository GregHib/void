package world.gregs.voidps.world.map.wilderness

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.weapon
import java.util.concurrent.TimeUnit

objectOperate("Slash", "web*") {
    if (player.weapon.def["slash_attack", 0] <= 0) {
        player.message("Only a sharp blade can cut through this sticky web.")
        cancel()
        return@objectOperate
    }
    slash(player, target)
}

itemOnObjectOperate(obj = "web*") {
    if (item.id == "knife" || item.def["slash_attack", 0] > 0) {
        player.message("Only a sharp blade can cut through this sticky web.")
        cancel()
        return@itemOnObjectOperate
    }
    slash(player, target)
}

fun slash(player: Player, target: GameObject) {
    player.setAnimation("dagger_slash")
    target.replace("web_slashed", ticks = TimeUnit.MINUTES.toTicks(1))
}
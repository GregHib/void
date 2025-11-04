package content.area.wilderness

import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class Web : Script {

    init {
        objectOperate("Pass", "web_spider", block = ::slash)
        objectOperate("Slash", "web", block = ::slash)
        itemOnObjectOperate(obj = "web*") { (target, item) ->
            if (item.id == "knife" || item.def["slash_attack", 0] > 0) {
                message("Only a sharp blade can cut through this sticky web.")
                return@itemOnObjectOperate
            }
            slash(this, target)
        }
    }

    fun slash(player: Player, interact: PlayerObjectInteract) {
        if (player.weapon.def["slash_attack", 0] <= 0) {
            player.message("Only a sharp blade can cut through this sticky web.")
            return
        }
        slash(player, interact.target)
    }

    fun slash(player: Player, target: GameObject) {
        player.anim("dagger_slash")
        target.replace("web_slashed", ticks = TimeUnit.MINUTES.toTicks(1))
    }
}

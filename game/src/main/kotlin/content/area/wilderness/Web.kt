package content.area.wilderness

import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class Web : Script {

    init {
        objectOperate("Pass", "web_spider", handler = ::slash)
        objectOperate("Slash", "web", handler = ::slash)
        itemOnObjectOperate(obj = "web*") { (target, item) ->
            if (!canSlash(item)) {
                message("Only a sharp blade can cut through this sticky web.")
                return@itemOnObjectOperate
            }
            slash(this, target)
        }
    }

    fun slash(player: Player, interact: PlayerOnObjectInteract) {
        if (!player.inventory.contains("knife") && !canSlash(player.weapon)) {
            player.message("Only a sharp blade can cut through this sticky web.")
            return
        }
        slash(player, interact.target)
    }

    fun slash(player: Player, target: GameObject) {
        player.anim("dagger_slash")
        if (!Random.nextBoolean()) {
            player.message("You fail to cut through it.")
            return
        }
        player.message("You slash the web apart.")
        target.replace("web_slashed", ticks = TimeUnit.MINUTES.toTicks(1))
    }

    private fun canSlash(item: Item): Boolean = item.id == "knife" || item.def["slash_attack", 0] > 0
}

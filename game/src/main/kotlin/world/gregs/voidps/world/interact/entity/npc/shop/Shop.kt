package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.player.Player

fun Player.hasShopSample(): Boolean = this["info_sample", false]

fun Player.shopContainer(sample: Boolean = hasShopSample()): Container {
    val shop: String = this["shop"]
    return container(if (sample) "${shop}_sample" else shop)
}
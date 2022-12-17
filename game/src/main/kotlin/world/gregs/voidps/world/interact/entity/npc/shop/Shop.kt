package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get

fun Player.hasShopSample(): Boolean = this["info_sample", false]

fun Player.shop(): String = this["shop"]

fun Player.shopContainer(sample: Boolean = hasShopSample()): Container {
    val shop: String = this["shop"]
    val name = if (sample) "${shop}_sample" else shop
    return if (name.endsWith("general_store")) {
        GeneralStores.bind(this, name)
    } else {
        containers.container(name)
    }
}
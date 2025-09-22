package content.skill.thieving

import world.gregs.voidps.engine.dispatch.ListDispatcher
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject

interface Stole {
    fun stole(player: Player, target: GameObject, item: Item) {}

    companion object : Stole {
        val dispatcher = ListDispatcher<Stole>()

        override fun stole(player: Player, target: GameObject, item: Item) {
            for (instance in dispatcher.instances) {
                instance.stole(player, target, item)
            }
        }
    }
}
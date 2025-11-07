package content.skill.thieving

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject

interface Stole {
    fun stole(block: (player: Player, target: GameObject, item: Item) -> Unit) {
        handlers.add(block)
    }

    companion object {
        private val handlers = mutableListOf<(Player, GameObject, Item) -> Unit>()

        fun stole(player: Player, target: GameObject, item: Item) {
            for (handler in handlers) {
                handler(player, target, item)
            }
        }

        fun clear() {
            handlers.clear()
        }
    }
}

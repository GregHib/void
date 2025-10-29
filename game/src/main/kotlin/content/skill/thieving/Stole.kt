package content.skill.thieving

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject

interface Stole {
    fun stole(block: (player: Player, target: GameObject, item: Item) -> Unit) {
        blocks.add(block)
    }

    companion object {
        val blocks = mutableListOf<(Player, GameObject, Item) -> Unit>()

        fun stole(player: Player, target: GameObject, item: Item) {
            for (instance in blocks) {
                instance(player, target, item)
            }
        }

        fun clear() {
            blocks.clear()
        }
    }
}

package content.skill.farming

import content.entity.sound.sound
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.ItemOn
import world.gregs.voidps.engine.entity.character.mode.interact.delay
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

@Script
class CompostBin : Api {

    @ItemOn("weeds", "compost_bin_empty,farming_compost_bin_#")
    override suspend fun operate(player: Player, id: String, item: Item, slot: Int, target: GameObject) {
        player.anim("take")
        player.sound("farming_putin")
        player.delay(2)
        val count = player.inventory.count("weeds")
        player.inventory.remove("weeds", count)
        repeat(count) {
            player.delay(2)
        }
        player.message("This compost bin contains compostable items (3/15).")
    }
}

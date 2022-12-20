import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.transact.remove
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.inc
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.skill.ItemOnItem

on<ItemOnItem>({ def.skill == Skill.Crafting && def.requires.any { it.id == "thread" } }) { player: Player ->
    val value = player.inc("thread_use", true)
    if (value == 5) {
        player.clear("thread_use")
        player.inventory.remove("thread")
        player.message("You use up one of your reels of thread.")
    }
}
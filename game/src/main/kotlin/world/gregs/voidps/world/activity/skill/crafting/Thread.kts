import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.incVar
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.remove
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.skill.ItemOnItem

on<ItemOnItem>({ def.skill == Skill.Crafting && def.requires.any { it.id == "thread" } }) { player: Player ->
    val value = player.incVar("thread_used")
    if (value == 5) {
        player.clearVar("thread_used")
        player.inventory.remove("thread")
        player.message("You use up one of your reels of thread.")
    }
}
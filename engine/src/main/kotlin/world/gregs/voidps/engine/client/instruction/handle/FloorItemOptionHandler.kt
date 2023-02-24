package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.clearInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.network.instruct.InteractFloorItem

class FloorItemOptionHandler(
    private val items: FloorItems,
    private val collisions: Collisions
) : InstructionHandler<InteractFloorItem>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractFloorItem) {
        val (id, x, y, optionIndex) = instruction
        val tile = player.tile.copy(x, y)
        val item = items[tile].firstOrNull { it.def.id == id && it.tile == tile }
        if (item == null) {
            logger.warn { "Invalid floor item $id $tile" }
            return
        }
        val options = item.def.floorOptions
        val selectedOption = options.getOrNull(optionIndex)
        if (selectedOption == null) {
            logger.warn { "Invalid floor item option $optionIndex ${options.contentToString()}" }
            return
        }
        if (selectedOption == "Examine") {
            player.message(item.def.getOrNull("examine") ?: return, ChatType.ItemExamine)
            return
        }
        player.clearInteract()
        player.mode = Interact(player, item, FloorItemOption(player, item, selectedOption), shape = if (collisions.check(tile, BLOCKED)) null else -1, approachRange = -1)
    }

    companion object {
        private const val BLOCKED = CollisionFlag.WALL_NORTH or
                CollisionFlag.WALL_EAST or
                CollisionFlag.WALL_SOUTH or
                CollisionFlag.WALL_WEST
    }
}
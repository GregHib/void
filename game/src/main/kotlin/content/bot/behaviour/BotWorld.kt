package content.bot.behaviour

import content.bot.behaviour.action.BotAction
import content.bot.behaviour.navigation.NavigationGraph
import content.bot.behaviour.navigation.NavigationShortcut
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.type.Tile

interface BotWorld {
    fun execute(player: Player, instruction: Instruction): Boolean

    fun shortcut(edge: Int): NavigationShortcut?

    fun actions(edge: Int): List<BotAction>?

    fun canTravel(tile: Tile, deltaX: Int, deltaY: Int) = canTravel(tile.x, tile.y, tile.level, deltaX, deltaY)

    fun canTravel(x: Int, y: Int, level: Int, deltaX: Int, deltaY: Int): Boolean

    fun find(player: Player, output: MutableList<Int>, area: String): Boolean

    fun findNearest(player: Player, output: MutableList<Int>, tag: String): Boolean
}

class BotGameWorld : BotWorld {
    val handlers: InstructionHandlers = get()
    val graph: NavigationGraph = get()
    val steps: StepValidator = get()

    override fun execute(player: Player, instruction: Instruction) = handlers.handle(player, instruction)

    override fun shortcut(edge: Int) = graph.shortcut(edge)

    override fun actions(edge: Int) = graph.actions(edge)

    override fun canTravel(x: Int, y: Int, level: Int, deltaX: Int, deltaY: Int) = steps.canTravel(level, x, y, deltaX, deltaY)

    override fun find(player: Player, output: MutableList<Int>, area: String) = graph.find(player, output, area)

    override fun findNearest(player: Player, output: MutableList<Int>, tag: String) = graph.findNearest(player, output, tag)
}

package content.bot.behaviour

import content.bot.behaviour.action.BotAction
import content.bot.behaviour.navigation.NavigationGraph
import content.bot.behaviour.navigation.NavigationShortcut
import org.rsmod.game.pathfinder.LineValidator
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.findPath
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
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

    /**
     * Whether [player] can walk to within interaction distance of [target]
     */
    fun canReach(player: Player, target: Character): Boolean = true

    /**
     * Whether [player] has line of sight to [target]
     */
    fun canSee(player: Player, target: Character): Boolean = true
}

class BotGameWorld : BotWorld {
    val handlers: InstructionHandlers = get()
    val graph: NavigationGraph = get()
    val steps: StepValidator = get()
    val pathFinder: PathFinder = get()
    val lineValidator: LineValidator = get()

    override fun execute(player: Player, instruction: Instruction) = handlers.handle(player, instruction)

    override fun shortcut(edge: Int) = graph.shortcut(edge)

    override fun actions(edge: Int) = graph.actions(edge)

    override fun canTravel(x: Int, y: Int, level: Int, deltaX: Int, deltaY: Int) = steps.canTravel(level, x, y, deltaX, deltaY)

    override fun find(player: Player, output: MutableList<Int>, area: String) = graph.find(player, output, area)

    override fun findNearest(player: Player, output: MutableList<Int>, tag: String) = graph.findNearest(player, output, tag)

    override fun canReach(player: Player, target: Character): Boolean {
        val route = pathFinder.findPath(player, TargetStrategy(player, target), null)
        return route.success && !route.alternative
    }

    override fun canSee(player: Player, target: Character) = lineValidator.hasLineOfSight(player, target)
}

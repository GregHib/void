package content.bot

import content.bot.behaviour.BotWorld
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.navigation.NavigationShortcut
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.Instruction

class FakeWorld(
    val execute: (Player, Instruction) -> Boolean = { _, _ -> false },
    val canTravel: (x: Int, y: Int, level: Int, deltaX: Int, deltaY: Int) -> Boolean = { _, _, _, _, _ -> false },
    val find: (player: Player, output: MutableList<Int>, area: String) -> Boolean = { _, _, _ -> false },
    val findNearest: (player: Player, output: MutableList<Int>, tag: String) -> Boolean = { _, _, _ -> false },
    val actions: (edge: Int) -> List<BotAction>? = { null },
    val shortcut: (edge: Int) -> NavigationShortcut? = { null },
) : BotWorld {
    override fun execute(player: Player, instruction: Instruction): Boolean = execute.invoke(player, instruction)

    override fun shortcut(edge: Int): NavigationShortcut? = shortcut.invoke(edge)

    override fun actions(edge: Int): List<BotAction>? = actions.invoke(edge)

    override fun canTravel(x: Int, y: Int, level: Int, deltaX: Int, deltaY: Int) = canTravel.invoke(x, y, level, deltaX, deltaY)

    override fun find(player: Player, output: MutableList<Int>, area: String): Boolean = find.invoke(player, output, area)

    override fun findNearest(player: Player, output: MutableList<Int>, tag: String): Boolean = findNearest.invoke(player, output, tag)
}

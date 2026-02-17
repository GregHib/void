package content.bot

import content.bot.behaviour.BotWorld
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.navigation.NavigationShortcut
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.Instruction

class FakeWorld : BotWorld {
    override fun execute(player: Player, instruction: Instruction): Boolean = false

    override fun shortcut(edge: Int): NavigationShortcut? = null

    override fun actions(edge: Int): List<BotAction>? = null

    override fun canTravel(x: Int, y: Int, level: Int, deltaX: Int, deltaY: Int): Boolean = false

    override fun find(player: Player, output: MutableList<Int>, area: String): Boolean = false

    override fun findNearest(player: Player, output: MutableList<Int>, tag: String): Boolean = false
}

package world.gregs.voidps.bot.navigation

import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.world.interact.entity.bot.goTo
import world.gregs.voidps.world.interact.entity.bot.steps

/*
    TODO
        remove tags from nav graph but leave mutable set
        replace values in areas with tags
        on startup check all graph nodes against areas and add tags
        also bind which area goes with which node? So can always keep track of which area a bot is in
        enjoy
 */
suspend fun Bot.goToNearest(tag: String) {
    player.goTo(tag)
    navigate()
}

suspend fun Bot.goToArea(area: MapArea) {
    player.goTo(area)
    navigate()
}

private suspend fun Bot.navigate() {
    if (player.steps?.isNotEmpty() == true || player.movement.waypoints.isNotEmpty()) {
        val steps = player.steps ?: return
        if (steps.isEmpty()) {
            for (next in player.movement.waypoints) {
                steps.addAll(next.steps)
            }
            player.movement.waypoints.clear()
        }
        while (steps.isNotEmpty()) {
            val step = steps.poll()
            this.step = step
            player.instructions.tryEmit(step)
            this.await<Unit>("move")
        }
        player["navigating"] = false
    }
}
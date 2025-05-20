package content.area.karamja.brimhaven

import content.skill.woodcutting.Hatchet
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit
import kotlin.random.Random

objectOperate("Chop-down", "brimhaven_vine_*") {
    val hatchet = Hatchet.best(player)
    if (hatchet == null || !Hatchet.hasRequirements(player, hatchet, true)) {
        player.message("You need a hatchet to cut through these vines.")
        return@objectOperate
    }

    if (!player.has(Skill.Woodcutting, 10, true)) {
        player.message("You need a Woodcutting level of at least 10 to chop through the vines.")
        return@objectOperate
    }

    player.queue("cutting_vine") {
        player.message("You swing your hatchet at the vines...")
        player.anim("${hatchet.id}_chop")
        delay(3)

        if (Random.nextInt(6) == 0) {
            player.message("You fail to cut the vines.")
            return@queue
        }

        player.message("You hack your way through the vines.")
        target.replace("${target.id}_cut", ticks = TimeUnit.SECONDS.toTicks(2))

        val direction = target.tile.delta(player.tile)
        player.walkOverDelay(target.tile.add(direction))
    }
}

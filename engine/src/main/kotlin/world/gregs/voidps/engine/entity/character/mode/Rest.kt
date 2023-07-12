package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.client.ui.playTrack
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation

class Rest(
    private val player: Player,
    private val track: Int
) : Mode {

    private val anim = animations.random()
    private val lastTrack = player["current_track", -1]

    override fun start() {
        player["movement_temp"] = player["movement", "walk"]
        player["movement"] = if (track != -1) "music" else "rest"
        player.setAnimation(anim)
        if (track != -1) {
            player.playTrack(track)
        }
    }

    override fun tick() {
    }

    override fun stop() {
        val type = player["movement_temp", "walk"]
        player["movement"] = type
        player.start("movement_delay", if (type == "walk") 2 else 1)
        if (lastTrack != -1) {
            player.playTrack(lastTrack)
        }
        player.setAnimation(anim.replace("rest", "stand"))
    }

    companion object {
        private val animations = setOf(
            "rest_arms_back",
            "rest_arms_crossed",
            "rest_legs_out"
        )
    }
}
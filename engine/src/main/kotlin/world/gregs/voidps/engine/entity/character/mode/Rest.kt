package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.entity.start

class Rest(
    private val player: Player,
    track: Int
) : Mode {

    private val anim = animations.random()
    private val lastTrack = player["current_track", -1]

    init {
        player["movement"] = player.getVar("movement", "walk")
        player.setVar("movement", if (track != -1) "music" else "rest")
        player.setAnimation(anim)
        /*if (track != -1) {
            player.playTrack(track)
        }*/
    }

    override fun tick() {
    }

    override fun stop() {
        val type = player["movement", "walk"]
        player.setVar("movement", type)
        player.start("rest_delay", if (type == "walk") 2 else 1)
        /*if (lastTrack != -1) {
            player.playTrack(lastTrack)
        }*/
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
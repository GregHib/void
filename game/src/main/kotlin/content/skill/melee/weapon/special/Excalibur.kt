package content.skill.melee.weapon.special

import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttackPrepare
import content.entity.sound.sound
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

@Script
class Excalibur : Api {

    @Key("sanctuary")
    override fun start(player: Player, timer: String, restart: Boolean) = 4

    @Key("sanctuary")
    override fun tick(player: Player, timer: String): Int {
        val cycle = player["sanctuary", 1] - 1
        player["sanctuary"] = cycle
        if (cycle <= 0) {
            return Timer.CANCEL
        }
        player.levels.restore(Skill.Constitution, 40)
        return Timer.CONTINUE
    }

    @Key("sanctuary")
    override fun stop(player: Player, timer: String, logout: Boolean) {
        player.clear("sanctuary")
    }

    init {
        specialAttackPrepare("sanctuary") { player ->
            cancel()
            if (!SpecialAttack.drain(player)) {
                return@specialAttackPrepare
            }
            player.anim("${id}_special")
            player.gfx("${id}_special")
            player.sound("${id}_special")
            player.say("For Camelot!")
            if (player.weapon.id.startsWith("enhanced")) {
                player.levels.boost(Skill.Defence, multiplier = 0.15)
                player[id] = TimeUnit.SECONDS.toTicks(if (seersVillageEliteTasks(player)) 24 else 12) / 4
                player.softTimers.start(id)
            } else {
                player.levels.boost(Skill.Defence, amount = 8)
            }
        }
    }

    fun seersVillageEliteTasks(player: Player) = false
}

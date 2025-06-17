package content.skill.constitution.drink

import content.skill.constitution.canConsume
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill

canConsume("zamorak_brew*", "zamorak_mix*") { player ->
    val health = player.levels.get(Skill.Constitution)
    val damage = ((health / 100) * 10) + 20
    if (health - damage < 0) {
        player.message("You need more hitpoints in order to survive the effects of the zamorak brew.")
        cancel()
    }
}

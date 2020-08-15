package rs.dusk.world.activity.skill.exp

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.EventCompanion
import rs.dusk.utility.get
import rs.dusk.world.activity.skill.Skill

data class Experience(override val player: Player, val skill: Skill, val increase: Int) : PlayerEvent() {
    companion object : EventCompanion<Experience>
}

fun Player.addExp(skill: Skill, increase: Int) {
    val bus: EventBus = get()
    bus.emit(Experience(this, skill, increase))
}
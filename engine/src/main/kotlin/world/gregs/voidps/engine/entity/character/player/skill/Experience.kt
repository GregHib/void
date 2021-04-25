package world.gregs.voidps.engine.entity.character.player.skill

import com.fasterxml.jackson.annotation.JsonIgnore
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Events

class Experience(
    val experience: DoubleArray = defaultExperience.clone(),
    val blocked: MutableSet<Skill> = mutableSetOf(),
    @JsonIgnore
    private val maximum: Double = MAXIMUM_EXPERIENCE
) {

    @JsonIgnore
    lateinit var events: Events

    fun get(skill: Skill): Double {
        return experience[skill.ordinal]
    }

    fun set(skill: Skill, experience: Double) {
        if (experience > 0.0 && experience <= maximum && !blocked.contains(skill)) {
            val previous = get(skill)
            this.experience[skill.ordinal] = experience
            update(skill, previous)
        }
    }

    fun update(skill: Skill, previous: Double = get(skill)) {
        val experience = get(skill)
        events.emit(GrantExp(skill, previous, experience))
    }

    fun add(skill: Skill, experience: Double) {
        if (experience > 0.0) {
            if (blocked.contains(skill)) {
                events.emit(BlockedExperience(skill, experience))
            } else {
                val current = get(skill)
                set(skill, current + experience)
            }
        }
    }

    fun addBlock(skill: Skill) {
        blocked.add(skill)
    }

    fun blocked(skill: Skill) = blocked.contains(skill)

    fun removeBlock(skill: Skill) {
        blocked.remove(skill)
    }

    companion object {
        const val MAXIMUM_EXPERIENCE = 200000000.0
        private val defaultExperience = DoubleArray(Skill.count) {
            if (it == Skill.Constitution.ordinal) {
                1154.0
            } else {
                0.0
            }
        }
    }
}

fun Player.exp(skill: Skill, experience: Double) = this.experience.add(skill, experience)
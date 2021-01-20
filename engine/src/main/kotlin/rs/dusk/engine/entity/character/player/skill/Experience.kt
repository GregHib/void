package rs.dusk.engine.entity.character.player.skill

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import rs.dusk.engine.entity.character.player.Player

@Serializable
class Experience(
    val experience: DoubleArray = defaultExperience.clone(),
    val blocked: MutableSet<Skill> = mutableSetOf(),
    @Transient
    private val maximum: Double = MAXIMUM_EXPERIENCE
) {
    @Transient
    private val listeners: MutableList<(Skill, Double, Double) -> Unit> = mutableListOf()
    @Transient
    private val blockedListeners: MutableList<(Skill, Double) -> Unit> = mutableListOf()

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

    fun notify(skill: Skill, experience: Double) {
        for (listener in blockedListeners) {
            listener.invoke(skill, experience)
        }
    }

    fun update(skill: Skill, previous: Double = get(skill)) {
        val experience = get(skill)
        for (listener in listeners) {
            listener.invoke(skill, previous, experience)
        }
    }

    fun add(skill: Skill, experience: Double) {
        if (experience > 0.0) {
            if (blocked.contains(skill)) {
                notify(skill, experience)
            } else {
                val current = get(skill)
                set(skill, current + experience)
            }
        }
    }

    fun addListener(listener: (Skill, from: Double, to: Double) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (Skill, Double, Double) -> Unit) {
        listeners.remove(listener)
    }

    fun addBlockedListener(listener: (Skill, experience: Double) -> Unit) {
        blockedListeners.add(listener)
    }

    fun removeBlockedListener(listener: (Skill, Double) -> Unit) {
        blockedListeners.remove(listener)
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
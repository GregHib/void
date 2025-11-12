package world.gregs.voidps.engine.entity.character.player.skill.exp

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.Skills
import world.gregs.voidps.engine.event.AuditLog

class Experience(
    val experience: IntArray = defaultExperience.clone(),
    val blocked: MutableSet<Skill> = mutableSetOf(),
) {

    lateinit var player: Player

    fun direct(skill: Skill): Int = experience[skill.ordinal]

    fun get(skill: Skill): Double = experience[skill.ordinal] / 10.0

    fun set(skill: Skill, experience: Double) {
        set(skill, (experience * 10.0).toInt())
    }

    fun set(skill: Skill, experience: Int) {
        if (experience in 0..MAXIMUM_EXPERIENCE && !blocked.contains(skill)) {
            val previous = direct(skill)
            this.experience[skill.ordinal] = experience
            update(skill, previous)
            AuditLog.event(player, "set_exp", skill, experience)
        }
    }

    fun update(skill: Skill, previous: Int = direct(skill)) {
        val experience = direct(skill)
        Skills.exp(player, skill, previous, experience)
    }

    fun add(skill: Skill, experience: Double) {
        if (experience <= 0.0) {
            return
        }
        val actual = experience * 10 * Settings["world.experienceRate", DEFAULT_EXPERIENCE_RATE]
        if (blocked.contains(skill)) {
            Skills.blocked(player, skill, actual.toInt())
        } else {
            val current = direct(skill)
            AuditLog.event(player, "exp", skill, experience)
            set(skill, current + actual.toInt())
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
        const val DEFAULT_EXPERIENCE_RATE = 1.0
        const val MAXIMUM_EXPERIENCE = 2_000_000_000
        val defaultExperience = IntArray(Skill.count) {
            if (it == Skill.Constitution.ordinal) 11540 else 0
        }

        fun level(skill: Skill, experience: Double): Int {
            var total = 0
            for (level in 1..if (skill == Skill.Dungeoneering) 120 else 99) {
                total += Level.experienceAt(level)
                if (experience < total / 4) {
                    return if (skill == Skill.Constitution) level * 10 else level
                }
            }
            return if (skill == Skill.Constitution) 990 else 99
        }
    }
}

fun Player.exp(skill: Skill, experience: Double) = this.experience.add(skill, experience)

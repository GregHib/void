package world.gregs.voidps.website.public

import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.get
import java.util.concurrent.atomic.AtomicLong

data class HighscoreEntry(
    val rank: Int,
    val name: String,
    val level: Int,
    val xp: Long,
    val rights: String,
    val mode: String,
)

data class PlayerStats(
    val name: String,
    val rights: String,
    val mode: String,
    val skills: Map<Skill, SkillStat>,
)

data class SkillStat(
    val level: Int,
    val xp: Long,
)

object Highscores {
    private var overallCache: List<HighscoreEntry> = emptyList()
    private var skillCaches: Map<Skill, List<HighscoreEntry>> = emptyMap()
    private val lastUpdate = AtomicLong(0)
    private const val UPDATE_INTERVAL = 300_000L // 5 minutes

    fun getEntries(skill: Skill? = null): List<HighscoreEntry> {
        val now = System.currentTimeMillis()
        if (now - lastUpdate.get() > UPDATE_INTERVAL) {
            refresh()
        }
        return if (skill == null) overallCache else skillCaches[skill] ?: emptyList()
    }

    fun getPlayerStats(name: String): PlayerStats? {
        val storage = get<Storage>()
        val save = storage.load(name) ?: return null

        val skills = Skill.all.associateWith { skill ->
            val rawXp = if (skill.ordinal < save.experience.size) save.experience[skill.ordinal] else 0
            val xp = rawXp.toDouble() / 10.0
            val level = Experience.level(skill, xp)
            SkillStat(if (skill == Skill.Constitution) level / 10 else level, xp.toLong())
        }

        val rights = (save.variables["rights"] as? String) ?: "None"
        val mode = (save.variables["game_mode"] as? String) ?: "Normal"

        return PlayerStats(save.name, rights, mode, skills)
    }

    @Synchronized
    fun refresh() {
        val now = System.currentTimeMillis()
        if (now - lastUpdate.get() < UPDATE_INTERVAL) return

        val storage = get<Storage>()
        val accountNames = storage.names().values.map { it.accountName }.distinct()

        val allPlayerSaves = accountNames.mapNotNull { storage.load(it) }

        // Overall
        overallCache = allPlayerSaves.map { save ->
            var totalLevel = 0
            var totalXp = 0L
            Skill.all.forEach { skill ->
                if (skill.ordinal < save.experience.size) {
                    val rawXp = save.experience[skill.ordinal]
                    totalXp += rawXp.toLong()
                    totalLevel += if (skill == Skill.Constitution) Experience.level(skill, rawXp / 10.0) / 10 else Experience.level(skill, rawXp / 10.0)
                }
            }
            val rights = (save.variables["rights"] as? String) ?: "None"
            val mode = (save.variables["game_mode"] as? String) ?: "Normal"
            HighscoreEntry(0, save.name, totalLevel, (totalXp / 10.0).toLong(), rights, mode)
        }
            .sortedWith(compareByDescending<HighscoreEntry> { it.level }.thenByDescending { it.xp })
            .mapIndexed { index, entry -> entry.copy(rank = index + 1) }

        // Individual Skills
        val newSkillCaches = mutableMapOf<Skill, List<HighscoreEntry>>()
        Skill.all.forEach { skill ->
            newSkillCaches[skill] = allPlayerSaves.map { save ->
                val rawXp = if (skill.ordinal < save.experience.size) save.experience[skill.ordinal] else 0
                val level = if (skill == Skill.Constitution) Experience.level(skill, rawXp / 10.0) / 10 else Experience.level(skill, rawXp / 10.0)
                val rights = (save.variables["rights"] as? String) ?: "None"
                val mode = (save.variables["game_mode"] as? String) ?: "Normal"
                HighscoreEntry(0, save.name, level, (rawXp.toDouble() / 10.0).toLong(), rights, mode)
            }
                .sortedByDescending { it.xp }
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
        }
        skillCaches = newSkillCaches
        lastUpdate.set(now)
    }
}

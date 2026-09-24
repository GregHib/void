package world.gregs.voidps.web.hiscores

import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.RecentEvent
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.web.PeriodicSnapshot
import world.gregs.voidps.web.api.model.*
import java.time.Instant
import java.util.IdentityHashMap
import kotlin.math.abs
import kotlin.math.floor

/**
 * Serves hiscores leaderboards and player profiles from a [PeriodicSnapshot] of [Storage] rebuilt
 * at most once per `refreshMillis`. Loading every account is far too slow to do per request, so
 * the snapshot loads them once, drops everything hiscores never reads (inventories, banks,
 * password hashes...) and precomputes every leaderboard along with each player's ranks, leaving
 * requests with a lookup and a page slice.
 *
 * There is no ironman/hardcore/ultimate account-type system in the engine yet, so every account is
 * reported as `standard`; the `mode` filter is kept in the API surface so the site's existing chips
 * keep working once one exists.
 */
class HiscoresService(
    private val storage: Storage,
    private val quests: QuestDefinitions,
    refreshMillis: Long = REFRESH_MS,
) {

    val bosses = NPCDefinitions.definitions
        .filter { it.getOrNull<Set<String>>("categories")?.contains("boss") == true }
        .associate { it.stringId to it.name }

    private val snapshot = PeriodicSnapshot("hiscores", refreshMillis) { rank(storage.accounts()) }

    private class Profile(val save: PlayerSave, val overallRank: Int, val skillRanks: Map<Skill, Int>, val bossRanks: Map<String, Int>)

    private class Rankings(
        val overall: List<Pair<PlayerSave, Int>>,
        val skills: Map<Skill, List<Pair<PlayerSave, Int>>>,
        val bossKills: Map<String, List<Pair<PlayerSave, Int>>>,
        /** Every recorded (account, team size, millis) per boss, fastest first. */
        val bossTimes: Map<String, List<Triple<PlayerSave, Int, Int>>>,
        /** Every account by display name, for the staff panel's search. */
        val byName: List<PlayerSave>,
        /** Keyed by lowercase display name and lowercase account name. */
        val profiles: Map<String, Profile>,
        val updatedAt: String,
    ) {
        fun find(name: String): Profile? = profiles[name.lowercase()]
    }

    private fun rank(loaded: List<PlayerSave>): Rankings {
        val accounts = loaded.map { it.slim() }
        val overall = rankOverall(accounts)
        val skills = Skill.all.associateWith { skill ->
            accounts
                .sortedWith(compareByDescending<PlayerSave> { it.skillXp(skill) }.thenBy { it.displayName().lowercase() })
                .mapIndexed { index, save -> save to index + 1 }
        }
        val bossKills = bosses.keys.associateWith { bossId ->
            accounts
                .filter { it.bossKills(bossId) > 0 }
                .sortedWith(compareByDescending<PlayerSave> { it.bossKills(bossId) }.thenBy { it.displayName().lowercase() })
                .mapIndexed { index, save -> save to index + 1 }
        }
        val bossTimes = bosses.keys.associateWith { bossId ->
            accounts.flatMap { save ->
                TEAM_SIZES.mapNotNull { size -> save.bossTimeMillis(bossId, size)?.let { millis -> Triple(save, size, millis) } }
            }.sortedBy { it.third }
        }
        val skillRanks = IdentityHashMap<PlayerSave, MutableMap<Skill, Int>>()
        for ((skill, ranked) in skills) {
            for ((save, rank) in ranked) {
                skillRanks.getOrPut(save) { mutableMapOf() }[skill] = rank
            }
        }
        val bossRanks = IdentityHashMap<PlayerSave, MutableMap<String, Int>>()
        for ((bossId, ranked) in bossKills) {
            for ((save, rank) in ranked) {
                bossRanks.getOrPut(save) { mutableMapOf() }[bossId] = rank
            }
        }
        val profiles = HashMap<String, Profile>()
        for ((save, rank) in overall) {
            val profile = Profile(save, rank, skillRanks[save] ?: emptyMap(), bossRanks[save] ?: emptyMap())
            profiles.putIfAbsent(save.displayName().lowercase(), profile)
            profiles.putIfAbsent(save.name.lowercase(), profile)
        }
        val byName = accounts.sortedBy { it.displayName().lowercase() }
        return Rankings(overall, skills, bossKills, bossTimes, byName, profiles, Instant.now().toString())
    }

    /** Every account in the snapshot ordered by display name; see [slim] for what's left out. */
    internal fun accountsByName(): List<PlayerSave> = snapshot.get().byName

    /** The account name belonging to a display or account [name], as of the last snapshot. */
    internal fun accountName(name: String): String? = snapshot.get().find(name)?.save?.name

    private val questPointsMax by lazy { quests.definitions.sumOf { it.questPoints.coerceAtLeast(0) } }

    private val metadata by lazy {
        HiscoresMetadata(
            updatedAt = "",
            maxTrackedXp = MAXIMUM_TRACKED_XP,
            skills = Skill.all.map {
                SkillMetadata(id = it.id(), name = it.name, maxLevel = it.displayMax(), iconUrl = it.iconUrl())
            },
            bosses = bosses.map { BossMetadata(it.key, it.value) },
            modes = MODES.map { ModeMetadata(id = it, name = it.replaceFirstChar(Char::uppercase)) },
            teamSizes = TEAM_SIZES,
        )
    }

    fun metadata(): HiscoresMetadata = metadata.copy(updatedAt = snapshot.get().updatedAt)

    fun overall(query: String?, mode: String?, page: Int, pageSize: Int): OverallLeaderboard {
        val rankings = snapshot.get()
        val filtered = filterByModeAndName(rankings.overall, mode, query)
        val (from, to) = window(filtered.size, page, pageSize)
        return OverallLeaderboard(
            pagination = Pagination.of(page, pageSize, filtered.size),
            updatedAt = rankings.updatedAt,
            items = filtered.subList(from, to).map { (save, rank) ->
                OverallRow(rank = rank, name = save.displayName(), mode = save.mode(), totalLevel = save.totalLevel(), totalXp = save.totalXp())
            },
        )
    }

    fun skill(skillId: String, query: String?, mode: String?, page: Int, pageSize: Int): SkillLeaderboard? {
        val skill = Skill.all.firstOrNull { it.name.equals(skillId, ignoreCase = true) } ?: return null
        val filtered = filterByModeAndName(snapshot.get().skills[skill] ?: emptyList(), mode, query)
        val (from, to) = window(filtered.size, page, pageSize)
        return SkillLeaderboard(
            skill = skill.id(),
            skillName = skill.name,
            maxLevel = skill.displayMax(),
            pagination = Pagination.of(page, pageSize, filtered.size),
            items = filtered.subList(from, to).map { (save, rank) ->
                SkillRow(rank = rank, name = save.displayName(), level = save.skillLevel(skill), xp = save.skillXp(skill))
            },
        )
    }

    fun bossKills(bossId: String, mode: String?, page: Int, pageSize: Int): BossKillLeaderboard? {
        val name = bosses[bossId] ?: return null
        val filtered = filterByModeAndName(snapshot.get().bossKills[bossId] ?: emptyList(), mode, null)
        val (from, to) = window(filtered.size, page, pageSize)
        return BossKillLeaderboard(
            boss = bossId,
            bossName = name,
            pagination = Pagination.of(page, pageSize, filtered.size),
            items = filtered.subList(from, to).map { (save, rank) -> BossKillRow(rank = rank, name = save.displayName(), kills = save.bossKills(bossId)) },
        )
    }

    fun bossTimes(bossId: String, teamSize: String?, page: Int, pageSize: Int): BossTimeLeaderboard? {
        val name = bosses[bossId] ?: return null
        val sizes = if (teamSize == null || teamSize == "all") TEAM_SIZES else listOfNotNull(teamSize.toIntOrNull()?.takeIf { it in TEAM_SIZES })
        val entries = (snapshot.get().bossTimes[bossId] ?: emptyList()).filter { it.second in sizes }
        val (from, to) = window(entries.size, page, pageSize)
        return BossTimeLeaderboard(
            boss = bossId,
            bossName = name,
            pagination = Pagination.of(page, pageSize, entries.size),
            items = entries.subList(from, to).mapIndexed { index, (save, size, millis) ->
                BossTimeRow(rank = from + index + 1, name = save.displayName(), teamSize = size, timeSeconds = millis / 1000.0)
            },
        )
    }

    fun searchPlayers(query: String?, limit: Int): List<PlayerSuggestion> {
        val q = query?.trim()?.lowercase()
        return snapshot.get().overall
            .asSequence()
            .filter { (save, _) -> q.isNullOrEmpty() || save.displayName().lowercase().contains(q) }
            .take(limit)
            .map { (save, rank) -> PlayerSuggestion(name = save.displayName(), rank = rank, totalLevel = save.totalLevel(), totalXp = save.totalXp(), mode = save.mode()) }
            .toList()
    }

    fun player(name: String): PlayerProfile? {
        val profile = snapshot.get().find(name) ?: return null
        val save = profile.save
        val maxedSkills = Skill.all.count { save.skillLevel(it) >= it.displayMax() }
        val bossKills = bosses.keys.sumOf { save.bossKills(it) }
        val lastEvent = save.recentEvents.maxByOrNull { it.time }
        return PlayerProfile(
            name = save.displayName(),
            mode = save.mode(),
            rights = save.rights(),
            overallRank = profile.overallRank,
            totalLevel = save.totalLevel(),
            totalXp = save.totalXp(),
            combatLevel = save.combatLevel(),
            questPoints = save.questPoints(),
            questPointsMax = questPointsMax,
            maxedSkills = maxedSkills,
            bossKills = bossKills,
            timePlayedHours = save.playtimeSeconds() / 3600.0,
            joinedAt = save.joinedAt(),
            lastSeenAt = lastEvent?.let { Instant.ofEpochSecond(it.time.toLong()).toString() },
            milestones = listOf(
                Milestone("Skills at 99", "$maxedSkills of ${Skill.count}"),
                Milestone("Quests complete", "${quests.ids.keys.count { save.questStatus(it) == "complete" }} of ${quests.ids.size}"),
                Milestone("Bosses defeated", bossKills.toString()),
                Milestone("Time played", "${(save.playtimeSeconds() / 3600)} h"),
                Milestone("Last seen", lastEvent?.let { Instant.ofEpochSecond(it.time.toLong()).toString() } ?: "—"),
            ),
        )
    }

    fun playerQuests(name: String, status: String?): PlayerQuests? {
        val save = snapshot.get().find(name)?.save ?: return null
        val list = quests.definitions
            .map { def ->
                val id = def.stringId
                val questStatus = save.questStatus(id)
                PlayerQuestRow(
                    id = id,
                    name = def.name ?: "",
                    difficulty = when (def.difficulty) {
                        0 -> "novice"
                        1 -> "intermediate"
                        2 -> "experienced"
                        3 -> "master"
                        4 -> "grandmaster"
                        else -> ""
                    },
                    status = questStatus,
                    questPoints = def.questPoints.coerceAtLeast(0),
                    completedAt = if (questStatus == "complete") save.questCompletedAt(def.name ?: "") else null,
                )
            }
            .filter { status == null || status == "all" || (status == "complete") == (it.status == "complete") }
        return PlayerQuests(
            completed = quests.ids.keys.count { save.questStatus(it) == "complete" },
            total = quests.ids.size,
            questPoints = save.questPoints(),
            questPointsMax = questPointsMax,
            items = list,
        )
    }

    fun playerEvents(name: String, type: String?, page: Int, pageSize: Int): PlayerEventPage? {
        val save = snapshot.get().find(name)?.save ?: return null
        val filtered = save.recentEvents
            .sortedByDescending { it.time }
            .filter { type == null || type == "all" || it.eventType() == type }
        val (from, to) = window(filtered.size, page, pageSize)
        return PlayerEventPage(
            pagination = Pagination.of(page, pageSize, filtered.size),
            items = filtered.subList(from, to).mapIndexed { index, event ->
                PlayerEventRow(
                    id = "${save.name.lowercase()}-${event.time}-$index",
                    type = event.eventType(),
                    text = event.title,
                    occurredAt = Instant.ofEpochSecond(event.time.toLong()).toString(),
                )
            },
        )
    }

    fun playerSkills(name: String): PlayerSkills? {
        val profile = snapshot.get().find(name) ?: return null
        val save = profile.save
        val items = Skill.all.map { skill ->
            PlayerSkillRow(
                skill = skill.id(),
                name = skill.name,
                level = save.skillLevel(skill),
                maxLevel = skill.displayMax(),
                xp = save.skillXp(skill),
                rank = profile.skillRanks[skill],
                progressPercent = (save.skillLevel(skill) * 100) / skill.displayMax(),
                iconUrl = skill.iconUrl(),
            )
        }
        return PlayerSkills(
            totalLevel = save.totalLevel(),
            totalXp = save.totalXp(),
            maxedCount = Skill.all.count { save.skillLevel(it) >= it.displayMax() },
            items = items,
        )
    }

    fun playerBosses(name: String): PlayerBosses? {
        val profile = snapshot.get().find(name) ?: return null
        val save = profile.save
        val items = bosses.map { (bossId, bossName) ->
            val fastest = TEAM_SIZES.mapNotNull { size -> save.bossTimeMillis(bossId, size) }.minOrNull()
            PlayerBossRow(boss = bossId, name = bossName, kills = save.bossKills(bossId), rank = profile.bossRanks[bossId], fastestSeconds = fastest?.let { it / 1000.0 })
        }
        return PlayerBosses(totalKills = items.sumOf { it.kills }, items = items)
    }

    fun compare(nameA: String, nameB: String): Comparison? {
        val rankings = snapshot.get()
        val profileA = rankings.find(nameA) ?: return null
        val profileB = rankings.find(nameB) ?: return null
        val a = profileA.save
        val b = profileB.save
        val rankA = profileA.overallRank
        val rankB = profileB.overallRank
        val bossKillsA = bosses.keys.sumOf { a.bossKills(it) }
        val bossKillsB = bosses.keys.sumOf { b.bossKills(it) }

        val skillRows = Skill.all.map { skill ->
            val aXp = a.skillXp(skill)
            val bXp = b.skillXp(skill)
            val leader = if (aXp == bXp) "tie" else if (aXp > bXp) "a" else "b"
            CompareSkillRow(
                skill = skill.id(),
                skillName = skill.name,
                a = SkillEntry(a.skillLevel(skill), aXp),
                b = SkillEntry(b.skillLevel(skill), bXp),
                leader = leader,
                differenceXp = abs(aXp - bXp),
            )
        }
        val bossRows = bosses.map { (bossId, bossName) ->
            val aKc = a.bossKills(bossId)
            val bKc = b.bossKills(bossId)
            val leader = if (aKc == bKc) "tie" else if (aKc > bKc) "a" else "b"
            CompareBossRow(boss = bossId, bossName = bossName, aKills = aKc, bKills = bKc, leader = leader, differenceKills = abs(aKc - bKc))
        }
        val skillsAhead = skillRows.count { it.leader == "a" }
        val summary = listOf(
            ComparisonSummaryCard(
                "Total experience",
                if (a.totalXp() >= b.totalXp()) a.displayName() else b.displayName(),
                "+${abs(a.totalXp() - b.totalXp())} xp",
            ),
            ComparisonSummaryCard(
                "Total level",
                if (a.totalLevel() >= b.totalLevel()) a.displayName() else b.displayName(),
                "${a.totalLevel()} · ${b.totalLevel()}",
            ),
            ComparisonSummaryCard(
                "Skills ahead",
                if (skillsAhead * 2 >= Skill.count) a.displayName() else b.displayName(),
                "$skillsAhead · ${Skill.count - skillsAhead} of ${Skill.count}",
            ),
            ComparisonSummaryCard(
                "Boss kills",
                if (bossKillsA >= bossKillsB) a.displayName() else b.displayName(),
                "$bossKillsA · $bossKillsB",
            ),
        )

        return Comparison(
            a = ComparisonSide(a.displayName(), a.mode(), rankA, a.totalLevel(), a.totalXp(), bossKillsA),
            b = ComparisonSide(b.displayName(), b.mode(), rankB, b.totalLevel(), b.totalXp(), bossKillsB),
            summary = summary,
            skills = skillRows,
            bosses = bossRows,
        )
    }

    private fun rankOverall(accounts: List<PlayerSave>): List<Pair<PlayerSave, Int>> =
        accounts
            .sortedWith(compareByDescending<PlayerSave> { it.totalXp() }.thenByDescending { it.totalLevel() }.thenBy { it.displayName().lowercase() })
            .mapIndexed { index, save -> save to index + 1 }

    private fun filterByModeAndName(ranked: List<Pair<PlayerSave, Int>>, mode: String?, query: String?): List<Pair<PlayerSave, Int>> {
        val q = query?.trim()?.lowercase()
        if ((mode == null || mode == "all") && q.isNullOrEmpty()) {
            return ranked
        }
        return ranked.filter { (save, _) ->
            (mode == null || mode == "all" || mode == save.mode()) && (q.isNullOrEmpty() || save.displayName().lowercase().contains(q))
        }
    }

    private fun window(total: Int, page: Int, pageSize: Int): Pair<Int, Int> {
        val from = (page * pageSize).coerceIn(0, total)
        val to = (from + pageSize).coerceIn(from, total)
        return from to to
    }

    companion object {
        const val REFRESH_MS = 1_200_000L
        const val MAXIMUM_TRACKED_XP = 200_000_000L
        val MODES = listOf("main", "skiller", "pure")
        val TEAM_SIZES = listOf(1, 2, 3, 4)
        private val TEAM_SUFFIXES = listOf(2 to "_duo", 3 to "_trio", 4 to "_quad")

        /** The skills a pure trains selectively and a skiller avoids entirely. */
        private val COMBAT_SKILLS = listOf(Skill.Attack, Skill.Strength, Skill.Defence, Skill.Magic, Skill.Ranged, Skill.Prayer, Skill.Summoning)

        /** Drops everything hiscores never reads so a snapshot of every account stays small (and holds no password hashes). */
        private fun PlayerSave.slim(): PlayerSave = copy(
            password = "",
            inventories = emptyMap(),
            friends = emptyMap(),
            ignores = emptyList(),
            offers = emptyArray(),
            history = emptyList(),
        )

        internal fun PlayerSave.displayName(): String = (variables["display_name"] as? String)?.takeIf { it.isNotBlank() } ?: name

        /**
         * There is no account-type system in the engine, so the type is derived from how many
         * combat skills are still untrained: all of them - a skiller, at least two - a pure,
         * otherwise a main.
         */
        private fun PlayerSave.mode(): String {
            val untrained = COMBAT_SKILLS.count { skillLevel(it) <= 1 }
            return when {
                untrained == COMBAT_SKILLS.size -> "skiller"
                untrained >= 2 -> "pure"
                else -> "main"
            }
        }

        internal fun PlayerSave.skillLevel(skill: Skill): Int {
            val raw = levels.getOrElse(skill.ordinal) { 1 }
            return if (skill == Skill.Constitution) raw / 10 else raw
        }

        internal fun PlayerSave.skillXp(skill: Skill): Long = experience.getOrElse(skill.ordinal) { 0 }.toLong() / 10

        internal fun PlayerSave.totalLevel(): Int = Skill.all.sumOf { skillLevel(it) }

        internal fun PlayerSave.totalXp(): Long = Skill.all.sumOf { skillXp(it) }

        internal fun PlayerSave.bossKills(id: String): Int = kills[id] ?: 0

        internal fun PlayerSave.bossTimeMillis(bossId: String, teamSize: Int): Int? {
            val key = if (teamSize == 1) bossId else (TEAM_SUFFIXES.firstOrNull { it.first == teamSize }?.second?.let { "$bossId$it" } ?: return null)
            return records[key]
        }

        /** "none", "mod" or "admin" - mirrors [world.gregs.voidps.engine.entity.character.player.PlayerRights]. */
        internal fun PlayerSave.rights(): String = (variables["rights"] as? String) ?: "none"

        private fun PlayerSave.questVariable(id: String): String = variables[id] as? String ?: "unstarted"

        private fun PlayerSave.questStatus(id: String): String = when {
            questVariable(id).startsWith("completed") -> "complete"
            questVariable(id) != "unstarted" -> "started"
            else -> "not-started"
        }

        internal fun PlayerSave.questPoints(): Int = (variables["quest_points"] as? Int) ?: 0

        internal fun PlayerSave.playtimeSeconds(): Int = (variables["playtime"] as? Int) ?: 0

        /** Best-effort: the most recent matching "Quest complete: <name>" entry still in [PlayerSave.recentEvents]. */
        private fun PlayerSave.questCompletedAt(name: String): String? {
            val title = "Quest complete: $name"
            val event = recentEvents.filter { it.title == title }.maxByOrNull { it.time } ?: return null
            return Instant.ofEpochSecond(event.time.toLong()).toString()
        }

        /** Inferred from the event's title, since [RecentEvent] doesn't record a kind of its own. */
        private fun RecentEvent.eventType(): String = when {
            title.startsWith("Quest complete", ignoreCase = true) || title.contains("Quest points", ignoreCase = true) -> "quest"
            title.contains("XP in", ignoreCase = true) || title.contains("total level", ignoreCase = true) || title.startsWith("Levelled", ignoreCase = true) -> "skill"
            title.contains("killed", ignoreCase = true) || title.contains("Dungeon floor", ignoreCase = true) || title.startsWith("I found", ignoreCase = true) -> "combat"
            else -> "account"
        }

        internal fun PlayerSave.joinedAt(): String? {
            val creation = variables["creation"]
            val millis = (creation as? Long) ?: (creation as? Int)?.toLong() ?: return null
            return Instant.ofEpochMilli(millis).toString()
        }

        internal fun PlayerSave.combatLevel(): Int {
            val defence = skillLevel(Skill.Defence)
            val hitpoints = skillLevel(Skill.Constitution)
            val prayer = skillLevel(Skill.Prayer)
            val attack = skillLevel(Skill.Attack)
            val strength = skillLevel(Skill.Strength)
            val ranged = skillLevel(Skill.Ranged)
            val magic = skillLevel(Skill.Magic)
            val base = (defence + hitpoints + prayer / 2) * 0.25
            val melee = (attack + strength) * 0.325
            val range = (ranged * 1.5) * 0.325
            val mage = (magic * 1.5) * 0.325
            return floor(base + maxOf(melee, range, mage)).toInt()
        }

        internal fun Skill.id(): String = name.lowercase()

        internal fun Skill.displayMax(): Int = if (this == Skill.Constitution) maximum() / 10 else maximum()

        private fun Skill.iconUrl(): String = "void/images/skills/${name.lowercase()}.png"
    }
}

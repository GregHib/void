package content.bot.combat

import world.gregs.voidps.engine.entity.character.player.skill.Skill

/**
 * Identity for a combat-bot kit: the activity the bot will be pinned to, the levels it should
 * be set to on spawn/respawn, and the combat style to select. Loaded from per-context tables
 * (e.g. clan_wars_tiers for [ClanWarsBotContext]); future contexts may define their own tables.
 */
data class CombatTier(
    val activityId: String,
    val levels: Map<Skill, Int>,
    val style: String,
)

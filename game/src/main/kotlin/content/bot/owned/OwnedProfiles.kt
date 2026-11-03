package content.bot.owned

import content.bot.Bot
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.action.BotAssistOwner
import content.bot.behaviour.action.BotDrinkPotion
import content.bot.behaviour.action.BotEatFood
import content.bot.behaviour.action.BotEscape
import content.bot.behaviour.action.BotFollowOwner
import content.bot.behaviour.action.BotOwnedHeal
import content.bot.behaviour.action.BotOwnedSpell
import content.bot.behaviour.action.BotPray
import content.bot.behaviour.action.BotSpecAttack
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotAllCondition
import content.bot.behaviour.condition.BotAnyCondition
import content.bot.behaviour.condition.BotAttackerStyle
import content.bot.behaviour.condition.BotCarrying
import content.bot.behaviour.condition.BotFighting
import content.bot.behaviour.condition.BotHasClock
import content.bot.behaviour.condition.BotSkillPercent
import content.bot.behaviour.condition.BotVariableIn
import content.skill.prayer.isCurses
import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * Builds a bot's follow activity from its owner's combat profile and knobs: the PvP bots' reactive stack
 * (eat, pray, drink, spec) under the owner's settings, plus assisting and escaping. Built per bot when it is
 * told to follow, so prayers and the spec fallback weapon match what the bot has right then.
 *
 * Profiles: `passive` eats and protects only; `melee`, `ranged` and `mage` add the offensive prayers and
 * boosts of that style the bot's prayer level allows. Every profile drinks Saradomin brews for hitpoints, but
 * only while it carries a super restore to undo the stat drain, and restores after three brews.
 * Between fights every bot eats back to full. Knobs: `spell` (the spellbook component it autocasts, else a mage
 * mirrors its owner's), `eat` (heal at this percent in a fight, 75), `flee` (escape at this percent, 25), `warn` (warn at this percent,
 * 40), `escape` (item id or prefix* to teleport with), `spec` (special attack weapon carried or worn).
 */
object OwnedProfiles {

    /**
     * The bot's combat behaviour around [actions] (following the owner by default; waiting in place for a hold; one
     * attack for an attack order, without [assist], so it keeps to its target), as the profile and knobs say.
     */
    fun build(owner: Player, bot: Bot, slot: Int, id: String = OwnedBots.FOLLOW_PREFIX, actions: List<BotAction> = listOf(BotFollowOwner()), assist: Boolean = true, timeout: Int = Int.MAX_VALUE): BotActivity {
        val player = bot.player
        val profile = OwnedRoster.profile(owner, slot)
        val knobs = OwnedRoster.knobs(owner, slot)
        val reactive = mutableListOf<BotAction>()
        val eat = knobs.int("eat", 75)
        reactive.add(BotEatFood(eat))
        reactive.add(BotOwnedHeal(topUp = true))
        // A brew heals but drains the combat stats, so only with a super restore left to undo it: at the eat
        // threshold, or straight after food for the extra hitpoints
        val restore = BotCarrying("super_restore_*")
        reactive.add(BotDrinkPotion("saradomin_brew_*", Skill.Constitution, BotAllCondition(listOf(restore, BotAnyCondition(listOf(BotSkillPercent(Skill.Constitution, maxPercent = eat), BotHasClock("just_ate_food")))))))
        reactive.add(BotEscape(warn = knobs.int("warn", 40), flee = knobs.int("flee", 25), item = knobs["escape"]))
        val curses = player.isCurses()
        val fighting = BotFighting()
        // Protection by whatever is hitting the bot
        for ((style, prayer) in listOf("melee" to "melee", "ranged" to "missiles", "magic" to "magic")) {
            val id = if (curses) "deflect_$prayer" else "protect_from_$prayer"
            if (available(player, id)) {
                reactive.add(BotPray(id, BotAttackerStyle(setOf(style))))
            }
        }
        val offensive = when (profile) {
            "melee" -> if (curses) listOf("turmoil") else listOf("piety", "chivalry", "ultimate_strength")
            "ranged" -> if (curses) listOf("turmoil") else listOf("rigour", "eagle_eye")
            "mage" -> if (curses) listOf("turmoil") else listOf("augury", "mystic_might")
            else -> emptyList()
        }
        offensive.firstOrNull { available(player, it) }?.let { reactive.add(BotPray(it, fighting)) }
        val starting = BotHasClock("fight_starting")
        when (profile) {
            "melee" -> {
                reactive.add(BotDrinkPotion("super_attack_*", Skill.Attack, starting))
                reactive.add(BotDrinkPotion("super_strength_*", Skill.Strength, starting))
                reactive.add(BotDrinkPotion("super_defence_*", Skill.Defence, starting))
                reactive.add(BotDrinkPotion("attack_potion_*", Skill.Attack, starting))
                reactive.add(BotDrinkPotion("strength_potion_*", Skill.Strength, starting))
            }
            "ranged" -> reactive.add(BotDrinkPotion("ranging_potion_*", Skill.Ranged, starting))
            "mage" -> reactive.add(BotDrinkPotion("magic_potion_*", Skill.Magic, starting))
        }
        if (profile != "passive") {
            reactive.add(BotDrinkPotion("prayer_potion_*", Skill.Prayer))
            reactive.add(BotDrinkPotion("super_restore_*", Skill.Prayer))
        }
        // Three brews down, or the profile's stat below a third: a restore puts the stats back
        val stat = when (profile) {
            "ranged" -> Skill.Ranged
            "mage" -> Skill.Magic
            "melee" -> Skill.Strength
            else -> Skill.Defence
        }
        reactive.add(BotDrinkPotion("super_restore_*", stat, BotAnyCondition(listOf(BotVariableIn("brew_doses_since_restore", 0, min = 3), BotSkillPercent(stat, maxPercent = 30)))))
        // The spell it autocasts: the owner's pick for it, else, as a mage, whatever the owner autocasts
        reactive.add(BotOwnedSpell(knobs["spell"], mirror = profile == "mage"))
        val spec = knobs["spec"]
        if (profile != "passive" && spec != null) {
            val worn = player.equipped(EquipSlot.Weapon).id
            reactive.add(BotSpecAttack(weapon = spec, fallback = if (worn.isBlank() || worn == spec) spec else worn, condition = fighting))
        }
        if (assist) {
            reactive.add(BotAssistOwner())
        }
        return BotActivity(
            id = id + player.accountName,
            timeout = timeout,
            actions = actions,
            reactive = reactive,
        )
    }

    /** The bot's prayer level allows [id]; unknown ids are skipped rather than failing every tick. */
    private fun available(player: Player, id: String): Boolean {
        val definition = get<PrayerDefinitions>().getOrNull(id) ?: return false
        return player.levels.getMax(Skill.Prayer) >= definition.level
    }

    private fun Map<String, String>.int(key: String, default: Int): Int = this[key]?.toIntOrNull()?.coerceIn(1, 99) ?: default
}

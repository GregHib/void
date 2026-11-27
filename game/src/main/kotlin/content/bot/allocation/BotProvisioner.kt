package content.bot.allocation

import com.github.michaelbull.logging.InlineLogger
import content.bot.Bot
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInventorySetup
import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttack
import content.entity.player.combat.special.specialAttackEnergy
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear

/**
 * Gives spawned role bots the levels and items needed for their [BotRole] so they can start straight away.
 * - Levels come from a [Build] rolled once and kept so re-provisioning (e.g. after death) gives the same levels.
 * - Gear comes from the role activity's loadout (worn), explicit equipment/inventory setup and hybrid loadout extras.
 */
class BotProvisioner(private val activity: (String) -> BotActivity?) {
    private val logger = InlineLogger("BotProvisioner")

    /**
     * Give [bot] the [role], rolling a new build.
     */
    fun provision(bot: Bot, role: BotRole, items: List<Pair<String, Int>> = emptyList()) {
        bot.role = role.id
        bot.buildLevels = role.pickBuild()?.roll() ?: emptyMap()
        bot.loadout = null
        restore(bot, role, items)
    }

    /**
     * Re-apply [bot]'s build levels, restore its stats and replace all of its items with the [role]'s kit plus [items].
     */
    fun restore(bot: Bot, role: BotRole, items: List<Pair<String, Int>> = emptyList()) {
        val player = bot.player
        for ((skill, level) in bot.buildLevels) {
            setLevel(player, skill, level)
        }
        val activity = activity(role.activity)
        val loadout = activity?.loadout?.pick(player, any = true, preferred = bot.loadout)
        loadout?.grantLevels(player)
        bot.loadout = loadout?.id

        player.levels.clear(Skill.Constitution)
        player.levels.clear(Skill.Prayer)
        player.specialAttackEnergy = MAX_SPECIAL_ATTACK
        player.specialAttack = false
        player["brew_doses_since_restore"] = 0
        player.stop("just_ate_food")
        if (role.combatStyle != null) {
            player["combat_style"] = role.combatStyle
        }

        player.inventory.transaction { clear() }
        player.equipment.transaction { clear() }
        player.inventory.transaction {
            for ((id, amount) in items) {
                add(id, amount)
            }
        }
        if (loadout != null && !loadout.equip(player)) {
            logger.warn { "Loadout '${loadout.id}' didn't fit for ${player.accountName}." }
        }
        if (activity != null) {
            equipSetup(player, activity)
        }
    }

    /**
     * Explicit equipment and inventory setup requirements along with the items for any switch loadouts not being worn
     */
    private fun equipSetup(player: Player, activity: BotActivity) {
        for (condition in activity.setup) {
            when (condition) {
                is BotEquipmentSetup -> player.equipment.transaction {
                    for ((slot, item) in condition.items) {
                        val id = item.primaryId ?: continue
                        set(slot.index, Item(id, item.min ?: 1))
                    }
                }
                is BotInventorySetup -> player.inventory.transaction {
                    for (item in condition.items) {
                        val id = item.primaryId ?: continue
                        add(id, item.min ?: 1)
                    }
                }
                else -> continue
            }
        }
        val starting = activity.startingLoadout
        if (starting != null) {
            player["current_loadout"] = starting
            player["last_loadout_swap_tick"] = -10_000
        }
        for ((name, loadout) in activity.switchLoadouts) {
            if (name == starting) {
                continue
            }
            if (!loadout.carry(player)) {
                logger.warn { "Loadout '$name' didn't fit for ${player.accountName}." }
            }
        }
    }

    private fun setLevel(player: Player, skill: Skill, level: Int) {
        val stored = if (skill == Skill.Constitution) level * 10 else level
        player.experience.set(skill, Level.experience(skill, stored))
        player.levels.set(skill, stored)
    }
}

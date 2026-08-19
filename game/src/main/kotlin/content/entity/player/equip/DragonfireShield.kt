package content.entity.player.equip

import content.entity.combat.Target
import content.entity.combat.dead
import content.entity.combat.hit.hit
import content.entity.combat.target
import content.entity.player.dialogue.type.statement
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.soundGlobal
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.charge
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.util.concurrent.TimeUnit

/**
 * The dragonfire shield absorbs the breath attacks it blocks, one charge each, and can spend a
 * charge to fire that dragonfire back at the current target.
 */
class DragonfireShield : Script {

    private val maximumRange = 12
    private val cooldown = TimeUnit.MINUTES.toTicks(2)
    private val attackDelay = 3
    private val smithingLevel = 90

    init {
        itemOption("Activate", "$CHARGED,$UNCHARGED", "worn_equipment", handler = ::activate)
        itemOption("Inspect", "$CHARGED,$UNCHARGED", handler = ::inspect)
        itemOption("Empty", CHARGED, handler = ::empty)
        combatDamage("dragonfire", handler = ::absorb)
        combatDamage("icy_breath", handler = ::absorb)
        itemOnObjectOperate("draconic_visage", "anvil*") { attachVisage() }
    }

    /**
     * Absorbs a breath attack into the shield, converting an uncharged shield into a charged one.
     */
    private fun absorb(player: Player, damage: CombatDamage) {
        if (damage.source !is NPC) {
            return
        }
        val index = EquipSlot.Shield.index
        when (player.equipment[index].id) {
            UNCHARGED -> player.equipment.replace(index, UNCHARGED, CHARGED)
            CHARGED -> player.equipment.charge(player, index, 1)
        }
    }

    private fun activate(player: Player, option: ItemOption) {
        if (option.item.id != CHARGED) {
            player.message("Your shield has no charges left.")
            return
        }
        if (player.hasClock("dragonfire_shield_cooldown")) {
            player.message("Your dragonfire shield is still recharging.")
            return
        }
        val target = player.target
        if (target == null || target.dead || !Target.attackable(player, target)) {
            player.message("You can only unleash the shield's dragonfire during combat.")
            return
        }
        if (player.tile.distanceTo(target) > maximumRange) {
            player.message("You are too far away from your target.")
            return
        }
        if (!player.equipment.discharge(player, EquipSlot.Shield.index, 1)) {
            return
        }
        player.start("dragonfire_shield_cooldown", cooldown)
        player.start("action_delay", maxOf(player.remaining("action_delay"), attackDelay))
        // The animation and the shield's graphic play together, the fire itself is held back by the
        // projectile's own delay while the shield winds up.
        player.anim("dragonfire_shield_discharge")
        player.gfx("dragonfire_shield_discharge_cast")
        player.soundGlobal("dragonfire_shield_discharge")
        val flight = player.shoot("dragonfire_shield_discharge", target)
        val damage = player.hit(target, weapon = option.item, offensiveType = "dragonfire", delay = flight, special = false)
        if (damage > 0) {
            target.gfx("dragonfire_shield_discharge_impact", delay = flight)
            areaSound("fire_strike_impact", target.tile, delay = flight, volume = 20)
        }
    }

    private fun inspect(player: Player, option: ItemOption) {
        val charges = player.inventories.inventory(option.inventory).charges(player, option.slot)
        if (charges <= 0) {
            player.message("The shield has no charges.")
            return
        }
        player.message("The shield has $charges ${"charge".plural(charges)}.")
    }

    private fun empty(player: Player, option: ItemOption) {
        val inventory = player.inventories.inventory(option.inventory)
        val charges = inventory.charges(player, option.slot)
        if (charges <= 0) {
            player.message("The shield has no charges.")
            return
        }
        if (!inventory.discharge(player, option.slot, charges)) {
            return
        }
        player.anim("dragonfire_shield_empty")
        player.gfx("dragonfire_shield_empty")
        player.soundGlobal("dragonfire_shield_empty")
        player.message("You release the charges.")
    }

    /**
     * Smiths a draconic visage onto an anti-dragon shield at an anvil.
     */
    private suspend fun Player.attachVisage() {
        if (!inventory.contains("anti_dragon_shield")) {
            statement("You need an anti-dragon shield to attach the visage to.")
            return
        }
        if (!has(Skill.Smithing, smithingLevel, message = false)) {
            statement("You need a Smithing level of $smithingLevel to do this.")
            return
        }
        if (!inventory.contains("hammer")) {
            statement("You need a hammer to work the metal with.")
            return
        }
        statement(
            """
            You set to work, trying to attach the ancient draconic
            visage to your anti-dragonbreath shield. It's not easy to
            work with the ancient artifact and it takes all of your
            skills as a master smith.
        """,
        )
        anim("smith_item")
        delay(4)
        val success = inventory.transaction {
            remove("draconic_visage")
            remove("anti_dragon_shield")
            add(UNCHARGED)
        }
        if (!success) {
            return
        }
        exp(Skill.Smithing, 2000.0)
        statement(
            """
            Even for an experienced armourer it is not an easy task, but
            eventually it is ready. You have crafted the draconic visage
            and anti-dragonbreath shield into a dragonfire shield.
        """,
        )
    }

    companion object {
        const val CHARGED = "dragonfire_shield_charged"
        const val UNCHARGED = "dragonfire_shield_uncharged"

        /**
         * Empties every dragonfire shield about to be lost on death, its charges don't survive the drop.
         */
        fun releaseCharges(player: Player) {
            releaseCharges(player, player.inventory)
            releaseCharges(player, player.equipment)
        }

        private fun releaseCharges(player: Player, inventory: Inventory) {
            for (index in inventory.items.indices) {
                if (inventory[index].id != CHARGED) {
                    continue
                }
                inventory.discharge(player, index, inventory.charges(player, index))
            }
        }
    }
}

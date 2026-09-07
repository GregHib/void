package content.minigame.duel_arena

import content.entity.player.combat.special.specialAttack
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.skill.melee.weapon.weapon
import content.skill.prayer.getActivePrayerVarKey
import content.skill.summoning.dismissFamiliar
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.markHint
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.type
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.move
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle

class DuelFight : Script {

    init {
        combatPrepare("magic") { _ ->
            allowed("no_magic", "Magic")
        }

        combatPrepare("range") { _ ->
            allowed("no_ranged", "Ranged")
        }

        combatPrepare("melee") { _ ->
            allowed("no_melee", "Melee")
        }

        combatPrepare { _ ->
            val duel = duel ?: return@combatPrepare true
            if (!duel.active || !duel.hasRule("fun_weapons") || DuelRules.funWeapon(weapon)) {
                return@combatPrepare true
            }
            message("This is a 'fun weapon' duel. You can only use flowers, basket of eggs, or a")
            message("rubber chicken.")
            false
        }

        variableSet("special_attack") { _, _, to ->
            if (to == true && duel?.active == true && duel?.hasRule("no_special") == true) {
                message("You can't use special attacks in this duel.")
                specialAttack = false
            }
        }

        teleportTakeOff("*") {
            if (duel?.active == true) {
                message("A magical force prevents you from teleporting from the arena.")
                return@teleportTakeOff false
            }
            true
        }

        droppable {
            if (duel?.active == true) {
                message("You cannot drop items whilst in a duel.")
                return@droppable false
            }
            true
        }

        playerLogout {
            if (duel?.active == true) {
                message("You can't log out during a duel.")
                return@playerLogout false
            }
            true
        }

        objectOperate("Forfeit", "duel_arena_forfeit_trapdoor", arrive = false) {
            val duel = duel ?: return@objectOperate
            if (duel.stage != DuelStage.Fighting) {
                message("The duel hasn't started yet.")
                return@objectOperate
            }
            if (duel.hasRule("no_forfeit")) {
                statement("Forfeit has been turned off for this duel.")
                return@objectOperate
            }
            val forfeit = choice(listOf("Yes.", "No."), "Do you wish to forfeit?")
            if (forfeit == 1 && duel.stage == DuelStage.Fighting) {
                DuelEnd.finish(duel, winner = duel.opponent(this), loser = this, DuelEnd.Result.Forfeit)
            }
        }
    }

    fun Player.allowed(rule: String, style: String): Boolean {
        val duel = duel ?: return true
        if (!duel.active || !duel.hasRule(rule)) {
            return true
        }
        message("$style has been turned off for this duel.")
        if (rule == "no_magic") {
            clear("spell")
        }
        return false
    }

    companion object {
        private const val COUNTDOWN_TICKS = 1

        fun start(duel: Duel) {
            duel.stage = DuelStage.Countdown
            duel.accepted.clear()
            val arena = arena(duel)
            val first = arena.random(duel.requester) ?: arena.random()
            val second = if (duel.hasRule("no_movement")) {
                adjacent(first, duel.acceptor, arena)
            } else {
                var tile = arena.random(duel.acceptor) ?: arena.random()
                if (tile == first) {
                    tile = adjacent(first, duel.acceptor, arena)
                }
                tile
            }
            setup(duel.requester, duel, first)
            setup(duel.acceptor, duel, second)
            duel.requester.face(duel.acceptor)
            duel.acceptor.face(duel.requester)
            countdown(duel, 3)
        }

        private fun arena(duel: Duel): Area {
            val tag = if (duel.hasRule("obstacles")) "obstacles" else "no_obstacles"
            return Areas.tagged("duel_spawn").filter { it.tags.contains(tag) }.random().area
        }

        /**
         * Tile next to [tile] for no-movement duels; players are always placed side-by-side, never diagonally
         */
        private fun adjacent(tile: Tile, player: Player, arena: Area): Tile {
            for (direction in listOf(Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH)) {
                val target = tile.add(direction)
                if (target !in arena) {
                    continue
                }
                if (Rectangle(target.x, target.y, target.x, target.y).random(player) != null) {
                    return target
                }
            }
            return tile.addX(-1)
        }

        private fun setup(player: Player, duel: Duel, tile: Tile) {
            val opponent = duel.opponent(player)
            player.closeMenu()
            removeEquipment(player, duel)
            if (duel.hasRule("no_prayer")) {
                player.clear(player.getActivePrayerVarKey())
            }
            if (duel.hasRule("no_drinks") || duel.hasRule("no_food")) {
                player.levels.clear()
            }
            player.dismissFamiliar()
            player.mode = EmptyMode
            player.steps.clear()
            player.tele(tile)
            player.interfaces.open("inventory")
            player["in_pvp"] = true
            player.options.remove("Challenge")
            player.options.set(ATTACK_SLOT, "Attack")
            player["equipment_tab"] = "forfeit"
            player.markHint(opponent)
            player.jingle("duel_start")
            if (duel.hasRule("no_food")) {
                player["no_food_message"] = "You cannot eat during this duel."
            }
            if (duel.hasRule("no_drinks")) {
                player["no_drinks_message"] = "You cannot drink during this duel."
            }
            if (duel.hasRule("no_prayer")) {
                player["no_prayer_message"] = "You can't use prayers in this duel."
            }
            if (!duel.hasRule("summoning")) {
                player["no_summoning_message"] = "Summoning has been disabled during this duel!"
            }
            if (duel.hasRule("no_movement")) {
                player["no_movement_message"] = "You cannot move during this duel!"
                player.start("movement_delay", -1)
            }
            val blocked = DuelRules.blockedSlots(duel)
            if (blocked.isNotEmpty()) {
                player["blocked_equip_slots"] = blocked
                player["blocked_equip_message"] = "You can't equip that during this duel."
            }
        }

        private fun removeEquipment(player: Player, duel: Duel) {
            for ((rule, slot) in DuelRules.equipment) {
                if (duel.hasRule(rule) && player.equipped(slot).isNotEmpty()) {
                    player.equipment.move(slot.index, player.inventory)
                }
            }
            if (duel.hasRule("no_shield") && player.equipped(EquipSlot.Weapon).type == EquipType.TwoHanded) {
                player.equipment.move(EquipSlot.Weapon.index, player.inventory)
            }
        }

        private fun countdown(duel: Duel, count: Int) {
            World.queue("duel_${duel.id}_countdown", COUNTDOWN_TICKS) {
                if (duel.stage != DuelStage.Countdown) {
                    return@queue
                }
                val text = if (count == 0) "FIGHT!" else count.toString()
                for (player in duel.players) {
                    player.say(text)
                }
                if (count == 0) {
                    duel.stage = DuelStage.Fighting
                } else {
                    countdown(duel, count - 1)
                }
            }
        }

        const val ATTACK_SLOT = 1
    }
}

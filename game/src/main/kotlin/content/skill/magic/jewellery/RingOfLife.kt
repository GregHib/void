package content.skill.magic.jewellery

import content.entity.combat.hit.combatDamage
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

@Script
class RingOfLife {

    init {
        combatDamage { player ->
            val attacker = this.source
            val damage = this.damage

            if (attacker == player || damage < 1) return@combatDamage

            val ring = player.equipped(EquipSlot.Ring)
            if (ring.id != "ring_of_life") return@combatDamage

            // Skip PvP and Duel Arena
            if (player.inPvpArea() || player.inDuelArena()) return@combatDamage

            val amulet = player.equipped(EquipSlot.Amulet)
            val hasPhoenix = amulet.id == "phoenix_necklace"

            val currentHp = player.levels.get(Skill.Constitution)
            val maxHp = player.levels.getMax(Skill.Constitution)
            val newHp = currentHp - damage

            // If Phoenix is equipped, let it handle >20% -> <=10% drops
            val droppedBelow10 = newHp > 0 && newHp <= (maxHp * 0.10)
            val wasAbove20 = currentHp > (maxHp * 0.20)
            if (hasPhoenix && droppedBelow10 && wasAbove20) return@combatDamage

            // Normal ring trigger (≤10%)
            if (newHp > 0 && newHp <= (maxHp * 0.10)) {
                activateRingOfLife(player)
            }
        }
    }

    private fun activateRingOfLife(player: Player) {
        player.message("Your Ring of Life glows brightly and saves you from certain death!")
        val destination = player["respawn_tile", Tile(3222, 3218)]
        player.teleport(destination, "modern")
        player.equipment.clear(EquipSlot.Ring.index)
        player.message("Your Ring of Life crumbles to dust.")
    }

    private fun Player.inPvpArea(): Boolean = this["pvp_zone", false]
    private fun Player.inDuelArena(): Boolean = this["duel_arena", false]
}
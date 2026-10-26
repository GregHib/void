package content.skill.slayer

import content.entity.combat.hit.Hit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class BlackMask : Script {

    init {
        // Remember monsters which attacked a player before the player attacked them
        npcCombatStart { target ->
            if (target is Player && !hasClock("under_attack")) {
                set("initiated_combat", target.index)
            }
        }

        npcCombatStop {
            clear("initiated_combat")
        }

        combatDamage { (source) ->
            if (source !is NPC || !equipped(EquipSlot.Hat).id.startsWith("black_mask_")) {
                return@combatDamage
            }
            if (random.nextInt(10) != 0 || !canDrain(source)) {
                return@combatDamage
            }
            if (!Hit.success(this, source, "magic", Item.EMPTY, special = false)) {
                return@combatDamage
            }
            if (equipment.discharge(this, EquipSlot.Hat.index)) {
                source.levels.drain(Skill.Defence, 2, 0.07)
            }
        }

        itemOption("Remove-charge", "black_mask_*") { (item, slot) ->
            inventory.replace(slot, item.id, "black_mask")
        }
    }

    private fun Player.canDrain(npc: NPC): Boolean = npc["initiated_combat", -1] == index &&
        npc.tile.within(tile, 10) &&
        npc.levels.get(Skill.Constitution) * 2 >= npc.levels.getMax(Skill.Constitution) &&
        npc.levels.get(Skill.Defence) >= npc.levels.getMax(Skill.Defence)
}

package content.area.asgarnia.asgarnian_ice_dungeon

import content.entity.combat.hit.Hit
import content.entity.combat.hit.hit
import content.entity.effect.freeze
import content.entity.effect.frozen
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class SkeletalWyvern : Script {

    init {
        npcImpact("skeletal_wyvern", "ice_breath") { target ->
            // 1/7 chance with proper shield
            if (hasWyvernShield(target)) random.nextInt(7) == 0 else Hit.success(this, target, "magic", Item.EMPTY, false)
        }
    }

    // TODO: fix blue ice orb for range as not as it is on runescape the blue ice orb is above his head not his chest

    fun hasWyvernShield(target: Character): Boolean {
        if (target !is Player) return false
        val shieldId = target.equipped(EquipSlot.Shield).id
        return shieldId == "elemental_shield" ||
            shieldId == "mind_shield" ||
            shieldId == "body_shield" ||
            shieldId.startsWith("dragonfire_shield")
    }
}

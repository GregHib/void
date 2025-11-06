package content.area.asgarnia.asgarnian_ice_dungeon

import content.entity.combat.hit.Hit
import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
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

    val specials = listOf("ice")

    init {
        npcCombatSwing("skeletal_wyvern") { target ->
            val canMelee = CharacterTargetStrategy(this).reached(target)
            when (random.nextInt(if (canMelee) 4 else 3)) {
                0 -> {
                    // Regular Ranged orb shot
                    anim("wyvern_ranged")
                    gfx("wyvern_ranged")
                    target.sound("wyvern_ranged")
                    nearestTile(this, target).shoot("wyvern_ranged", target)
                    hit(target, offensiveType = "range")
                }
                1 -> {
                    // Ice breath
                    val type = specials.random()
                    anim("wyvern_ice_breath")
                    gfx("wyvern_ice_breath")
                    target.sound("wyvern_ice_breath")
                    nearestTile(this, target).shoot("wyvern_ice_breath$type", target)
                    // Always apply hit splash visual
                    target.gfx("wyvern_ice_breath_hit")
                    hit(target, offensiveType = "icy_breath", spell = "ice", special = true)
                }
                2 -> {
                    // Melee tail whip
                    anim("wyvern_tail_whip")
                    target.sound("wyvern_tail_whip")
                    hit(target, offensiveType = "melee")
                }
                3 -> {
                    // Melee orb melee variant (if seen in video)
                    anim("skeletal_wyvern_defend")
                    gfx("wyvern_ranged")
                    target.sound("wyvern_ranged")
                    hit(target, offensiveType = "melee")
                }
            }
        }

        npcCombatAttack("skeletal_wyvern") { npc ->
            if (spell == "ice" && special) {
                val hasShield = hasSpecificWyvernShield(target)
                val shouldFreeze = if (hasShield) {
                    random.nextInt(7) == 0 // 1/7 chance with proper shield
                } else {
                    Hit.success(npc, target, "magic", Item.EMPTY, false)
                }

                if (shouldFreeze && !target.frozen) {
                    val baseFreeze = 10
                    target.freeze(baseFreeze)
                    target.message("The wyvern's icy breath chills you to the bone!")
                }
            }
        }
    }

    // TODO: fix blue ice orb for range as not as it is on runescape the blue ice orb is above his head not his chest

    fun nearestTile(source: Character, target: Character): Tile {
        val half = source.size / 2
        val centre = source.tile.add(half, half)
        val direction = target.tile.delta(centre).toDirection()
        return centre.add(direction).add(direction)
    }

    fun hasSpecificWyvernShield(target: Character): Boolean {
        if (target !is Player) return false
        val shieldId = target.equipped(EquipSlot.Shield).id
        return shieldId == "elemental_shield" ||
                shieldId == "mind_shield" ||
                shieldId == "body_shield" ||
                shieldId.startsWith("dragonfire_shield")
    }
}

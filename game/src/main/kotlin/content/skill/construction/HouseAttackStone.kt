package content.skill.construction

import content.entity.combat.hit.Damage
import content.entity.effect.transform
import content.skill.melee.attackAnimation
import content.skill.melee.weapon.Weapon
import content.skill.melee.weapon.attackStyle
import content.skill.melee.weapon.attackType
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.WeaponAnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.flagHits
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.network.login.protocol.visual.update.HitSplat
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction

/**
 * Games room attack stones: set up a stone to hit until it cracks and finally shatters.
 * Clay, limestone and marble stones take progressively more hits to wear down.
 * https://oldschool.runescape.wiki/w/Games_room
 */
class HouseAttackStone(val animationDefinitions: WeaponAnimationDefinitions, val styleDefinitions: WeaponStyleDefinitions) : Script {
    init {
        objectOperate("Set-up", STONES.keys.joinToString(",")) { (target) ->
            if (get("house_build_mode", false)) {
                message("You cannot set up an attack stone while in building mode.")
                return@objectOperate
            }
            val type = STONES.getValue(target.id)
            message("You activate the stone.", ChatType.Filter)
            areaSound("poh_setup_stone", target.tile, radius = 5)
            val stone = NPCs.add((FIRST + type * IDS_PER_STONE).toString(), target.tile, forward(target.rotation))
            stone["stone_id"] = target.id
            stone["stone_type"] = type
            // Npcs spawn at the start of the next tick, so the stone stays until then rather than leaving a gap
            delay(1)
            target.replace("invisible_seat", collision = false)
        }

        npcOperate("Hit", (FIRST..LAST).joinToString(",")) { (target) ->
            hit(target)
        }
    }

    private suspend fun Player.hit(stone: NPC) {
        face(stone)
        anim(attackAnimation(animationDefinitions, styleDefinitions))
        sound(
            when {
                equipped(EquipSlot.Weapon).isEmpty() -> "unarmed_punch"
                attackType == "slash" -> "stabsword_slash"
                else -> "stabsword_stab"
            },
        )
        delay(1)
        areaSound("poh_clay_hit", stone.tile, radius = 5)
        val type: Int = stone["stone_type", 0]
        val offensiveType = Weapon.type(this, weapon)
        val before: Int = stone["stone_damage", 0]
        val roll = Damage.roll(this, stone, offensiveType, weapon)
        val damage = if (roll < 0) 0 else Damage.modify(this, stone, offensiveType, roll, weapon, "").coerceIn(0, HITPOINTS - before)
        val total = before + damage
        stone["stone_damage"] = total
        val remaining = (HITPOINTS - total).coerceAtLeast(0)
        stone.visuals.hits.add(HitSplat(damage, if (damage == 0) HitSplat.Mark.Missed else Weapon.mark(offensiveType), remaining * 255 / HITPOINTS, source = index))
        stone.flagHits()
        grantExperience(damage * EXPERIENCE[type])
        if (total >= HITPOINTS) {
            message("You shatter the stone!", ChatType.Filter)
            areaSound("poh_stone_shatter", stone.tile, radius = 5)
            val id: String = stone["stone_id", ""]
            NPCs.remove(stone)
            GameObjects.findOrNull(stone.tile, "invisible_seat")?.replace(id)
            return
        }
        val stage = total * STAGES / HITPOINTS
        if (stage != before * STAGES / HITPOINTS) {
            stone.transform((FIRST + type * IDS_PER_STONE + stage * IDS_PER_STAGE).toString())
        }
    }

    // The direction the stone object faces, so the npc replacing it doesn't turn on spawn
    private fun forward(rotation: Int) = when (rotation and 0x3) {
        0 -> Direction.WEST
        1 -> Direction.NORTH
        2 -> Direction.EAST
        else -> Direction.SOUTH
    }

    /**
     * Experience for the damage dealt goes to the skills of the attack style used
     */
    private fun Player.grantExperience(experience: Double) {
        if (experience <= 0) {
            return
        }
        when (attackStyle) {
            "accurate" -> exp(Skill.Attack, experience)
            "aggressive" -> exp(Skill.Strength, experience)
            "defensive" -> exp(Skill.Defence, experience)
            "controlled" -> {
                exp(Skill.Attack, experience / 3)
                exp(Skill.Strength, experience / 3)
                exp(Skill.Defence, experience / 3)
            }
        }
    }

    companion object {
        private val STONES = mapOf(
            "clay_attack_stone" to 0,
            "limestone_attack_stone" to 1,
            "marble_attack_stone" to 2,
        )

        // The first combat stone npc with something to hit, each stone has four npcs for each of its four stages
        private const val FIRST = 3957
        private const val LAST = 4020
        private const val IDS_PER_STAGE = 4
        private const val IDS_PER_STONE = 16
        private const val STAGES = 4

        // Damage needed to shatter a stone in tenths of a hit point, each point of damage is worth 2.5% of the experience of destroying the stone
        private const val HITPOINTS = 400
        private val EXPERIENCE = doubleArrayOf(0.25, 0.5, 5.0)
    }
}

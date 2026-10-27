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

/**
 * Games room attack stones: set up a stone to hit until it cracks and finally shatters.
 * Clay, limestone and marble stones take progressively more hits to wear down.
 * https://oldschool.runescape.wiki/w/Games_room
 */
class HouseAttackStone(val animationDefinitions: WeaponAnimationDefinitions) : Script {
    private val hitpoints = 400
    private val experienceRates = doubleArrayOf(0.25, 0.5, 5.0)

    init {
        objectOperate("Set-up", "*_attack_stone") { (target) ->
            if (get("house_build_mode", false)) {
                message("You cannot set up an attack stone while in building mode.")
                return@objectOperate
            }
            val type = when (target.id) {
                "limestone_attack_stone" -> 1
                "marble_attack_stone" -> 2
                else -> 0
            }
            message("You activate the stone.", ChatType.Filter)
            areaSound("poh_setup_stone", target.tile, radius = 5)
            // Face the same way as the stone object so the npc doesn't turn on spawn
            val stone = NPCs.add("poh_tbt_limestone_${type}00", target.tile, target.direction().rotate(-2))
            stone["stone_id"] = target.id
            stone["stone_type"] = type
            // Npcs spawn at the start of the next tick, so the stone stays until then rather than leaving a gap
            delay(1)
            target.replace("invisible_seat", collision = false)
        }

        npcOperate("Hit", "poh_tbt_limestone_*") { (target) ->
            hit(target)
        }
    }

    private suspend fun Player.hit(stone: NPC) {
        face(stone)
        anim(attackAnimation(animationDefinitions))
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
        val damage = if (roll < 0) 0 else Damage.modify(this, stone, offensiveType, roll, weapon, "").coerceIn(0, hitpoints - before)
        val total = before + damage
        stone["stone_damage"] = total
        val remaining = (hitpoints - total).coerceAtLeast(0)
        stone.visuals.hits.add(HitSplat(damage, if (damage == 0) HitSplat.Mark.Missed else Weapon.mark(offensiveType), remaining * 255 / hitpoints, source = index))
        stone.flagHits()
        grantExperience(damage * experienceRates[type])
        if (total >= hitpoints) {
            message("You shatter the stone!", ChatType.Filter)
            areaSound("poh_stone_shatter", stone.tile, radius = 5)
            val id: String = stone["stone_id", ""]
            NPCs.remove(stone)
            GameObjects.findOrNull(stone.tile, "invisible_seat")?.replace(id)
            return
        }
        val stage = total * 4 / hitpoints
        if (stage != before * 4 / hitpoints) {
            stone.transform("poh_tbt_limestone_${type}${stage}0")
        }
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
}

package content.area.asgarnia.burthorpe.warriors_guild

import content.area.asgarnia.burthorpe.warriors_guild.WarriorsGuild.Companion.earnTokens
import content.skill.melee.weapon.attackStyle
import content.skill.melee.weapon.attackType
import content.skill.melee.weapon.combatStyle
import content.skill.melee.weapon.fightStyle
import content.skill.melee.weapon.weapon
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.data.definition.WeaponAnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.random

/**
 * Dummies pop up one at a time, hitting one with the matching attack style or type rewards tokens.
 * https://runescape.wiki/w/Warriors%27_Guild#Dummy_Room
 */
class DummyRoom(
    val styleDefinitions: WeaponStyleDefinitions,
    val animationDefinitions: WeaponAnimationDefinitions,
) : Script {

    private var round = 0
    private var active: RowDefinition? = null
    private val dummies = mutableListOf<GameObject>()

    init {
        worldSpawn {
            World.timers.start("warriors_guild_dummies")
        }

        worldTimerStart("warriors_guild_dummies") { RAISED_TICKS }

        worldTimerTick("warriors_guild_dummies") {
            when (active) {
                null -> raise()
                else -> lower()
            }
        }

        objectOperate("Hit", "dummy_stab,dummy_slash,dummy_crush,dummy_controlled,dummy_defensive,dummy_aggressive,dummy_accurate") { (target) ->
            val type = target.id.removePrefix("dummy_")
            if (active?.rowId != type) {
                return@objectOperate
            }
            if (fightStyle != "melee") {
                message("You can only use melee against the dummies.") // TODO proper message
                return@objectOperate
            }
            if (get("warriors_guild_dummy_round", -1) == round) {
                message("You have already hit a dummy this turn.")
                return@objectOperate
            }
            face(target)
            anim(attackAnimation())
            if (!correctStyle(type)) {
                message("You whack the dummy with the wrong attack style.")
                return@objectOperate
            }
            set("warriors_guild_dummy_round", round)
            exp(Skill.Attack, 15.0)
            earnTokens(2)
            message("You whack the dummy successfully!")
        }

        objectOperate("View", "dummy_room_sign") {
            open("dummy_room_sign")
        }
    }

    private fun raise(): Int {
        val row = Tables.get("warriors_guild_dummies").rows().random(random)
        val id = row.obj("obj")
        val rotations = row.intList("rotations")
        for ((index, tile) in row.tileList("tiles").withIndex()) {
            dummies.add(GameObjects.add(id, tile, rotation = rotations[index], ticks = RAISED_TICKS + 1))
        }
        round++
        active = row
        return RAISED_TICKS
    }

    private fun lower(): Int {
        for (dummy in dummies) {
            dummy.anim("dummy_sink")
        }
        dummies.clear()
        active = null
        return LOWERED_TICKS
    }

    private fun Player.correctStyle(type: String): Boolean = when (type) {
        "stab", "slash", "crush" -> combatStyle == type
        else -> attackStyle == type
    }

    private fun Player.attackAnimation(): String {
        val type: String? = weapon.def.getOrNull("weapon_type")
        val definition = if (type != null) animationDefinitions.get(type) else null
        val animation = definition?.attackTypes?.getOrDefault(Params.id(attackType), definition.attackTypes[Params.DEFAULT])
        if (animation != null) {
            return animation
        }
        val style = styleDefinitions.get(weapon.def["weapon_style", 0])
        return "${style.stringId}_$attackType"
    }

    companion object {
        private const val RAISED_TICKS = 10
        private const val LOWERED_TICKS = 4
    }
}

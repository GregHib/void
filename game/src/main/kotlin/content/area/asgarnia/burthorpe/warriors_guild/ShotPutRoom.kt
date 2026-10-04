package content.area.asgarnia.burthorpe.warriors_guild

import content.area.asgarnia.burthorpe.warriors_guild.WarriorsGuild.Companion.earnTokens
import content.entity.combat.hit.damage
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.energyPercent
import content.entity.player.effect.energy.runEnergy
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random
import kotlin.math.ceil

/**
 * Throw an 18lb or 22lb shot as far as possible for tokens and strength experience.
 * https://runescape.wiki/w/Warriors%27_Guild#Shot_Put_Room
 */
class ShotPutRoom : Script {

    init {
        objectOperate("Throw", "shot_put_18lb,shot_put_22lb") { (target) ->
            if (has(EquipSlot.Weapon) || has(EquipSlot.Shield) || has(EquipSlot.Hands)) {
                statement("To throw the shot you need your hands free!")
                return@objectOperate
            }
            if (energyPercent() < 10) {
                statement("You're too exhausted to throw the shot at this time. Take a break.")
                return@objectOperate
            }
            val heavy = target.id == "shot_put_22lb"
            anim("human_pickupfloor")
            delay(2)
            face(Direction.EAST)
            choice("Choose your style") {
                option("Standing throw") {
                    throwShot(heavy, "shot_put_standing_throw", 5, "You throw the shot as hard as you can.")
                }
                option("Step and throw") {
                    throwShot(heavy, "shot_put_step_and_throw", 2, "You take a step and throw the shot as hard as you can.")
                }
                option("Spin and throw") {
                    throwShot(heavy, "shot_put_spin_and_throw", 5, "You spin around and release the shot.")
                }
            }
        }

        itemOption("Dust-hands", "ground_ashes") { (item, slot) ->
            if (tile !in Areas["warriors_guild_shot_put_north"] && tile !in Areas["warriors_guild_shot_put_south"]) {
                message("You may only dust your hands while in the shotput throwing areas.")
                return@itemOption
            }
            if (!inventory.remove(slot, item.id)) {
                return@itemOption
            }
            set("warriors_guild_hands_dusted", true)
            message("You dust your hands with the finely ground ash.")
        }

        takeable("18lb_shot,22lb_shot") { _, _ ->
            npc<Angry>("ref", "Hey! You can't take that, it's guild property. Take one from the pile.")
            null
        }
    }

    private suspend fun Player.throwShot(heavy: Boolean, animation: String, throwDelay: Int, throwMessage: String) {
        val distance = distance(heavy)
        val fumbled = distance < 2
        message("You take a deep breath and prepare yourself.")
        anim(animation)
        delay(throwDelay)
        runEnergy -= MAX_RUN_ENERGY / 10
        message(throwMessage)
        if (fumbled) {
            message("You fumble and drop the shot on your toe. Ow!")
            damage(10)
        }
        val landing = tile.addX(distance)
        tile.shoot("shot_put_shot", landing)
        delay(1 + ceil(distance * 0.3).toInt())
        FloorItems.add(landing, if (heavy) "22lb_shot" else "18lb_shot", disappearTicks = 20, owner = this)
        if (fumbled) {
            return
        }
        exp(Skill.Strength, distance.toDouble())
        earnTokens(if (heavy) distance + 2 else distance)
        val yards = distance - 1
        npc<Happy>("ref", "Well done. You threw the shot $yards yard${if (yards == 1) "" else "s"}!")
    }

    /**
     * Throw distance is reduced by the weight of the shot and how tired the player is, dusted hands negate both
     */
    private fun Player.distance(heavy: Boolean): Int {
        var cost = if (heavy) 12 else 6
        if (get("warriors_guild_hands_dusted", false)) {
            clear("warriors_guild_hands_dusted")
            cost = 0
        }
        var distance = 1
        if (random.nextInt(25 - cost) != 0) {
            val fatigue = if (cost == 0) 0 else (random.nextInt(cost) * (1 - energyPercent() / (100.0 + cost))).toInt()
            distance += random.nextInt(12 - fatigue)
        }
        return distance
    }
}

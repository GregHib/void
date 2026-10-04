package content.area.asgarnia.burthorpe.warriors_guild

import content.area.asgarnia.burthorpe.warriors_guild.WarriorsGuild.Companion.earnTokens
import content.entity.combat.hit.damage
import content.entity.player.dialogue.type.statement
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.energyPercent
import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.equip.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

/**
 * Jimmy's challenge: balance up to five kegs on your head for as long as your energy lasts.
 * https://runescape.wiki/w/Warriors%27_Guild#Jimmy's_Challenge
 */
class KegBalance : Script {

    init {
        objectOperate("Pick-up", "warriors_guild_kegs") {
            if (energyPercent() < 5) {
                statement("You're too exhausted to continue. Take a break.")
                return@objectOperate
            }
            val kegs = get("warriors_guild_kegs", 0)
            if (has(EquipSlot.Weapon) || has(EquipSlot.Shield) || has(EquipSlot.Hands) || equipped(EquipSlot.Hat).id != keg(kegs)) {
                statement("To balance kegs you will need your head and hands free!")
                return@objectOperate
            }
            if (kegs >= KEGS.size) {
                message("You can't balance any more kegs on your head.") // TODO proper message
                return@objectOperate
            }
            anim("keg_pick_up")
            message("You pick up the keg and balance it on your head carefully.")
            delay(3)
            equipment.transaction {
                set(EquipSlot.Hat.index, Item(KEGS[kegs]))
            }
            set("warriors_guild_kegs", kegs + 1)
            set("movement", "walk")
            running = false
            softTimers.startIfAbsent("warriors_guild_kegs")
        }

        itemOption("Remove", KEGS.joinToString(","), "worn_equipment") {
            softTimers.stop("warriors_guild_kegs")
        }

        timerStart("warriors_guild_kegs") { BALANCE_TICKS }

        timerTick("warriors_guild_kegs") {
            val kegs = get("warriors_guild_kegs", 0)
            if (kegs == 0 || equipped(EquipSlot.Hat).id != keg(kegs) || has(EquipSlot.Weapon) || has(EquipSlot.Shield) || has(EquipSlot.Hands)) {
                return@timerTick Timer.CANCEL
            }
            runEnergy -= MAX_RUN_ENERGY / 20
            if (running || random.nextInt(100) > energyPercent() - 5 * kegs) {
                say("Ouch!")
                message("Some of the barrels hit you on their way to the floor.")
                gfx("keg_fall_$kegs")
                damage(10)
                return@timerTick Timer.CANCEL
            }
            earnTokens(kegs)
            Timer.CONTINUE
        }

        timerStop("warriors_guild_kegs") {
            dropKegs()
        }

        exited("warriors_guild_keg_room") {
            softTimers.stop("warriors_guild_kegs")
        }

        playerSpawn {
            if (equipped(EquipSlot.Hat).id in KEGS) {
                dropKegs()
            }
        }
    }

    private fun Player.dropKegs() {
        if (equipped(EquipSlot.Hat).id in KEGS) {
            equipment.transaction {
                set(EquipSlot.Hat.index, null)
            }
        }
        clear("warriors_guild_kegs")
    }

    companion object {
        private const val BALANCE_TICKS = 5
        private val KEGS = listOf("one_barrel", "two_barrels", "three_barrels", "four_barrels", "five_barrels")

        private fun keg(count: Int) = if (count == 0) "" else KEGS[count - 1]
    }
}

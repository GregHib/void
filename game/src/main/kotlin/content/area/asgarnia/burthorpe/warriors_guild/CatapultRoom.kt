package content.area.asgarnia.burthorpe.warriors_guild

import content.area.asgarnia.burthorpe.warriors_guild.WarriorsGuild.Companion.earnTokens
import content.entity.combat.hit.damage
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.player.equip.Equipping
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.equip.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.move
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Stand on the target wielding a defensive shield and choose the right defence for each catapult attack.
 * https://runescape.wiki/w/Warriors%27_Guild#Catapult_Room
 */
class CatapultRoom : Script {

    init {
        npcOperate("Claim-shield", "gamfred") {
            if (inventory.contains(SHIELD) || equipment.contains(SHIELD)) {
                npc<Neutral>("You already have a shield.") // TODO proper message
                return@npcOperate
            }
            if (!inventory.add(SHIELD)) {
                npc<Neutral>("You don't have enough space for a shield.") // TODO proper message
                return@npcOperate
            }
            npc<Happy>("Here you go, stand on the target and wield it to defend yourself from the catapult.") // TODO proper message
        }

        itemOption("Wield", SHIELD) { (item, slot) ->
            if (tile != TARGET) {
                message("You may not equip this shield outside the target area in the Warrior's Guild.")
                return@itemOption
            }
            if (has(EquipSlot.Weapon)) {
                statement("You will need to make sure your sword hand is free to equip this shield.")
                return@itemOption
            }
            if (!Equipping.equip(this, item, slot)) {
                return@itemOption
            }
            open("catapult_defence")
            tab(Tab.Inventory)
        }

        interfaceOption(id = "catapult_defence:*") {
            set("warriors_guild_catapult_defence", it.component)
        }

        entered("warriors_guild_catapult_target") {
            softTimers.start("warriors_guild_catapult")
        }

        exited("warriors_guild_catapult_target") {
            softTimers.stop("warriors_guild_catapult")
        }

        timerStart("warriors_guild_catapult") { FIRE_TICKS }

        timerTick("warriors_guild_catapult") {
            if (tile != TARGET) {
                return@timerTick Timer.CANCEL
            }
            if (equipped(EquipSlot.Shield).id != SHIELD) {
                warn()
                return@timerTick Timer.CANCEL
            }
            fire()
            Timer.CONTINUE
        }

        timerStop("warriors_guild_catapult") {
            removeShield()
        }

        slotChanged("worn_equipment", EquipSlot.Shield) { change ->
            if (change.fromItem.id == SHIELD && change.item.id != SHIELD) {
                close("catapult_defence")
                open("inventory")
            }
        }

        objectOperate("View", "catapult_room_sign") {
            open("catapult_room_sign")
        }
    }

    private fun Player.warn() {
        queue("warriors_guild_catapult_warning") {
            npc<Angry>("gamfred", "Watch out! You'll need to equip the shield as soon as you're on the target spot else you could get hit! Speak to me to get one, and make sure both your hands are free to equip it.")
        }
        walkTo(tile.addX(1))
    }

    private fun Player.fire() {
        val attack = Tables.get("warriors_guild_catapult_attacks").rows().random(random)
        CATAPULT.shoot("catapult_${attack.rowId}", TARGET)
        val catapult = GameObjects.at(CATAPULT_OBJECT).firstOrNull()
        catapult?.replace(attack.obj("obj"), ticks = 4)
        queue("warriors_guild_catapult_hit", HIT_DELAY) {
            if (tile != TARGET || equipped(EquipSlot.Shield).id != SHIELD) {
                return@queue
            }
            face(CATAPULT)
            val name = attack.rowId.replace("_", " ")
            if (get("warriors_guild_catapult_defence", "") != attack.rowId) {
                anim(attack.anim("fail"))
                message("You fail defending against the $name.")
                damage(30) // Guessed damage
                return@queue
            }
            anim(attack.anim("success"))
            exp(Skill.Defence, 10.0)
            earnTokens(1)
            message("You successfully defend against the $name.")
        }
    }

    private fun Player.removeShield() {
        val index = EquipSlot.Shield.index
        if (equipment[index].id == SHIELD) {
            equipment.move(index, inventory)
        }
        clear("warriors_guild_catapult_defence")
        if (interfaces.contains("catapult_defence")) {
            close("catapult_defence")
            open("inventory")
        }
    }

    companion object {
        private const val SHIELD = "defensive_shield"
        private const val FIRE_TICKS = 10
        private const val HIT_DELAY = 7
        val TARGET = Tile(2842, 3545, 1)
        private val CATAPULT = Tile(2842, 3554, 1)
        private val CATAPULT_OBJECT = Tile(2840, 3552, 1)
    }
}

package content.quest.member.monkey_madness

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeType
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Greegrees(val items: FloorItems) : Script {

    init {
        playerSpawn {
            val item = equipped(EquipSlot.Weapon).id
            if (item.endsWith("_greegree")) {
                if (tile in Areas["ape_atoll"] || tile in Areas["ape_atoll_agility_dungeon"]) {
                    transform(item.replace("_greegree", ""))
                    closeType("spellbook_tab")
                } else {
                    forceRemove()
                }
            }
        }

        itemAdded("*_greegree", "worn_equipment", EquipSlot.Weapon) { (item) ->
            val sound = when {
                item.id.endsWith("gorilla_greegree") -> "human_into_gorilla"
                item.id.endsWith("zombie_monkey_greegree") -> "human_into_zombie_monkey"
                item.id.startsWith("small") -> "human_into_small_monkey"
                else -> "human_into_monkey"
            }
            sound(sound)
            gfx("monkey_transform")
            transform(item.id.replace("_greegree", ""))
            closeType("spellbook_tab")
        }

        itemRemoved("*_greegree", "worn_equipment", EquipSlot.Weapon) {
            if (!equipment[it.index].id.endsWith("_greegree")) {
                val item = it.item
                val sound = when {
                    item.id.endsWith("gorilla_greegree") -> "gorilla_into_human"
                    item.id.endsWith("zombie_monkey_greegree") -> "zombie_monkey_into_human"
                    item.id.startsWith("small") -> "small_monkey_into_human"
                    else -> "monkey_into_human"
                }
                sound(sound)
                gfx("monkey_transform")
                clearTransform()
                val book = get("spellbook_config", 0) and 0x3
                open(
                    when (book) {
                        1 -> "ancient_spellbook"
                        2 -> "lunar_spellbook"
                        3 -> "dungeoneering_spellbook"
                        else -> "modern_spellbook"
                    },
                )
            }
        }

        exited("ape_atoll") {
            forceRemove()
        }

        exited("ape_atoll_agility_dungeon") {
            forceRemove()
        }
    }

    fun Player.forceRemove() {
        if (get("logged_out", false)) {
            return // TODO check if removed on logout or not
        }
        if (tile in Areas["ape_atoll"] || tile in Areas["ape_atoll_agility_dungeon"]) {
            return
        }
        val item = equipped(EquipSlot.Weapon).id
        if (item.endsWith("_greegree")) {
            softQueue("remove_greegree") {
                statement("The Monkey Greegree wrenches itself from your hand as its power begins to fade...")
            }
            if (!equipment.move(EquipSlot.Weapon.index, inventory)) {
                if (equipment.remove(EquipSlot.Weapon.index, item)) {
                    // FIXME issue with item spawning displaying twice if spawned on the same tick. #614
                    World.queue("greegree_spawn", 1) {
                        items.add(tile, item, disappearTicks = 300, owner = this)
                    }
                }
            }
        }
    }
}

package content.quest.member.monkey_madness

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.type.statement
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeType
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Greegrees : Script {

    val items: FloorItems by inject()
    val areas: AreaDefinitions by inject()

    init {
        playerSpawn { player ->
            val item = player.equipped(EquipSlot.Weapon).id
            if (item.endsWith("_greegree")) {
                if (player.tile in areas["ape_atoll"] || player.tile in areas["ape_atoll_agility_dungeon"]) {
                    player.transform(item.replace("_greegree", ""))
                    player.closeType("spellbook_tab")
                } else {
                    forceRemove(player)
                }
            }
        }

        itemAdded("*_greegree", EquipSlot.Weapon, "worn_equipment") { player ->
            val sound = when {
                item.id.endsWith("gorilla_greegree") -> "human_into_gorilla"
                item.id.endsWith("zombie_monkey_greegree") -> "human_into_zombie_monkey"
                item.id.startsWith("small") -> "human_into_small_monkey"
                else -> "human_into_monkey"
            }
            player.sound(sound)
            player.gfx("monkey_transform")
            player.transform(item.id.replace("_greegree", ""))
            player.closeType("spellbook_tab")
        }

        itemRemoved("*_greegree", EquipSlot.Weapon, "worn_equipment") { player ->
            if (!player.equipment[index].id.endsWith("_greegree")) {
                val sound = when {
                    item.id.endsWith("gorilla_greegree") -> "gorilla_into_human"
                    item.id.endsWith("zombie_monkey_greegree") -> "zombie_monkey_into_human"
                    item.id.startsWith("small") -> "small_monkey_into_human"
                    else -> "monkey_into_human"
                }
                player.sound(sound)
                player.gfx("monkey_transform")
                player.clearTransform()
                val book = player["spellbook_config", 0] and 0x3
                player.open(
                    when (book) {
                        1 -> "ancient_spellbook"
                        2 -> "lunar_spellbook"
                        3 -> "dungeoneering_spellbook"
                        else -> "modern_spellbook"
                    },
                )
            }
        }

        exitArea("ape_atoll") {
            forceRemove(player)
        }

        exitArea("ape_atoll_agility_dungeon") {
            forceRemove(player)
        }
    }

    fun forceRemove(player: Player) {
        if (player["logged_out", false]) {
            return // TODO check if removed on logout or not
        }
        if (player.tile in areas["ape_atoll"] || player.tile in areas["ape_atoll_agility_dungeon"]) {
            return
        }
        val item = player.equipped(EquipSlot.Weapon).id
        if (item.endsWith("_greegree")) {
            player.softQueue("remove_greegree") {
                statement("The Monkey Greegree wrenches itself from your hand as its power begins to fade...")
            }
            if (!player.equipment.move(EquipSlot.Weapon.index, player.inventory)) {
                if (player.equipment.remove(EquipSlot.Weapon.index, item)) {
                    // FIXME issue with item spawning displaying twice if spawned on the same tick. #614
                    World.queue("greegree_spawn", 1) {
                        items.add(player.tile, item, disappearTicks = 300, owner = player)
                    }
                }
            }
        }
    }
}

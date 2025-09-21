package content.area.asgarnia.port_sarim

import content.entity.obj.ship.boatTravel
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.type.Tile

@Script
class MonkOfEntrana {

    init {
        npcOperate("Talk-to", "monk_of_entrana_port_sarim*") {
            npc<Quiz>("Do you seek passage to holy Entrana? If so, you must leave your weaponry and armour behind. This is Saradomin's will.")
            choice("What would you like to say?") {
                option<Talk>("No, not right now.") {
                    npc<Talk>("Very well.")
                }
                option<Happy>("Yes, okay, I'm ready to go.") {
                    npc<Talk>("Very well. One moment please.")
                    player.message("The monk quickly searches you.")
                    statement("", clickToContinue = false)
                    delay(3)
                    if (passedCheck()) {
                        npc<Talk>("All is satisfactory. You may board the boat now.")
                        travel()
                    }
                }
            }
        }

        npcOperate("Take-boat", "monk_of_entrana_port_sarim*") {
            if (passedCheck()) {
                player.message("After a quick, search, the monk smiles at you and allows you to board.")
                travel()
            }
        }

        npcOperate("Talk-to", "entrana_monk*") {
            npc<Happy>("Do you wish to leave holy Entrana?")
            choice {
                option<Talk>("Yes, I'm ready to go.") {
                    npc<Happy>("Okay, let's board...")
                    portSarim()
                }
                option("Not just yet.")
            }
        }

        npcOperate("Take-boat", "entrana_monk*") {
            player.message("The ship takes you to Port Sarim.")
            portSarim()
        }
    }

    private val bannedCategories = setOf(
        "throwable",
        "arrow",
        "bolt",
        "magic_armour",
        "magic_weapon",
        "melee_armour_low",
        "melee_armour_mid",
        "melee_armour_high",
        "melee_weapon_low",
        "melee_weapon_mid",
        "melee_weapon_high",
        "prayer_armour",
        "prayer_consumable",
        "range_armour",
        "range_weapon",
    )

    private suspend fun NPCOption<Player>.passedCheck(): Boolean {
        var forbidden = itemCheck(player.inventory)
        if (forbidden.isEmpty()) {
            forbidden = itemCheck(player.equipment)
        }
        if (forbidden.isNotEmpty()) {
            npc<Angry>("NO WEAPONS OR ARMOUR are permitted on holy Entrana AT ALL. We will not allow you to travel there in breach of mighty Saradomin's edict.")
            if (forbidden.def.getOrNull<String>("god") == "saradomin") {
                npc<Talk>("I'm sorry, sir, but no weapons or armour may be worn on Entrana. This rule even forbids items dedicated to Saradomin.")
            } else {
                npc<Talk>("Do not try and deceive us again. Come back when you have laid down your Zamorakian instruments of death.")
            }
            return false
        }
        return true
    }

    private fun itemCheck(inventory: Inventory): Item {
        for (item in inventory.items) {
            if (item.isEmpty()) {
                continue
            }
            val categories: Set<String> = item.def.getOrNull("categories") ?: continue
            if (bannedCategories.any { categories.contains(it) }) {
                return item
            }
        }
        return Item.EMPTY
    }

    private suspend fun SuspendableContext<Player>.travel() {
        boatTravel("port_sarim_to_entrana", 14, Tile(2834, 3331, 1))
        statement("The ship arrives at Entrana.")
    }

    private suspend fun SuspendableContext<Player>.portSarim() {
        boatTravel("entrana_to_port_sarim", 14, Tile(3048, 3231, 1))
        statement("The ship arrives at Port Sarim.")
    }
}

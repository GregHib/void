package content.skill.construction

import content.area.asgarnia.falador.openDressingRoom
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * Bedroom furniture: shoe boxes to change shoes, drawers and wardrobes to change clothes or shoes and shaving stands and dressers to change hairstyles
 * https://oldschool.runescape.wiki/w/Bedroom
 */
class HouseBedroom : Script {
    init {
        objectOperate("Change-clothes", "shoe_box") {
            changeShoes()
        }

        objectOperate("Change-clothes", "oak_drawers,oak_wardrobe,teak_drawers,teak_wardrobe,mahogany_wardrobe,gilded_wardrobe") {
            choice("What would you like to change?") {
                // TODO proper message
                option("Clothes") {
                    changeClothes()
                }
                option("Shoes") {
                    changeShoes()
                }
            }
        }

        objectOperate("Preen", "shaving_stand,oak_shaving_stand,oak_dresser,teak_dresser,fancy_teak_dresser,mahogany_dresser,gilded_dresser") {
            changeHair()
        }
    }

    private suspend fun Player.changeClothes() {
        if (!equipment.isEmpty()) {
            message("You need to take off your armour before changing your clothes.") // TODO proper message
            return
        }
        openDressingRoom("thessalias_makeovers")
    }

    private suspend fun Player.changeShoes() {
        if (equipped(EquipSlot.Weapon).isNotEmpty() || equipped(EquipSlot.Shield).isNotEmpty() || equipped(EquipSlot.Feet).isNotEmpty()) {
            message("You need to empty your hands and take off your boots before changing your shoes.") // TODO proper message
            return
        }
        openDressingRoom("yrsas_shoe_store")
    }

    private suspend fun Player.changeHair() {
        val hat = equipped(EquipSlot.Hat)
        if (hat.isNotEmpty()) {
            item(hat.id, "You should take off your hat if you want to get a haircut!")
            return
        }
        openDressingRoom("hairdressers_salon")
    }
}

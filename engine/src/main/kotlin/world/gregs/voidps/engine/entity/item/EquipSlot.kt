package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.player.Player

enum class EquipSlot(val index: Int) {
    None(-1),
    Hat(0),
    Cape(1),
    Amulet(2),
    Weapon(3),
    Chest(4),
    Shield(5),
    Legs(7),
    Hands(9),
    Feet(10),
    Ring(12),
    Arrows(13),
    Aura(14);

    companion object {
        fun by(index: Int): EquipSlot = values().firstOrNull { it.index == index } ?: None

        fun by(name: String): EquipSlot {
            val name = name.capitalize()
            return values().firstOrNull { it.name == name } ?: None
        }

    }
}

fun Player.equipped(slot: EquipSlot): Item = equipment.getItem(slot.index)
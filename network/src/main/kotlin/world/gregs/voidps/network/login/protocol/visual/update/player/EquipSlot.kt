package world.gregs.voidps.network.login.protocol.visual.update.player

import java.util.*

enum class EquipSlot(val index: Int) {
    None(-1),
    Hat(0),// Head
    Cape(1),
    Amulet(2),
    Weapon(3),
    Chest(4),
    Shield(5),
    Legs(7),
    Hands(9),
    Feet(10),
    Ring(12),
    Ammo(13);

    companion object {
        fun by(index: Int): EquipSlot = entries.firstOrNull { it.index == index } ?: None

        fun by(name: String): EquipSlot {
            val formatted = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            return entries.firstOrNull { it.name == formatted } ?: None
        }

    }
}
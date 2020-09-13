package rs.dusk.engine.entity.item.detail

import rs.dusk.engine.entity.EntityDetail
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType
import rs.dusk.engine.entity.item.ItemDrop

/**
 * @param id Item id
 * @param slot Item equipment slot
 * @param type Item equipment body appearance override
 * @param equip Equip model index
 * @param weight
 * @param edible Can be eaten
 * @param tradeable Can be traded
 * @param alchable Can be turned into gold with alchemy
 * @param bankable Can be placed into a bank
 * @param individual Can't be stacked in a bank
 * @param limit Grand exchange buy limit
 * @param demise Item action on death
 * @param destroy Destroy message
 * @param examine Examine message
 */
data class ItemDetail(
    val id: Int,
    val slot: EquipSlot = EquipSlot.None,
    val type: EquipType = EquipType.None,
    val equip: Int = -1,
    val weight: Double = 0.0,
    val edible: Boolean = false,
    val tradeable: Boolean = true,
    val alchable: Boolean = true,
    val bankable: Boolean = true,
    val individual: Boolean = false,
    val limit: Int = -1,
    val demise: ItemDrop = ItemDrop.Drop,
    val destroy: String = "",
    val examine: String = ""
) : EntityDetail
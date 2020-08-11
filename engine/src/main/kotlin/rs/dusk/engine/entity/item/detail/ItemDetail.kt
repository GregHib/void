package rs.dusk.engine.entity.item.detail

import rs.dusk.engine.entity.EntityDetail
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType

data class ItemDetail(val id: Int, val slot: EquipSlot = EquipSlot.None, val type: EquipType = EquipType.None, val equip: Int = -1) : EntityDetail
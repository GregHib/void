import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.BodyParts
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.entity.item.BodyPart
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.event.on

on<ItemChanged>({ container == "worn_equipment" && needsUpdate(index, it.appearance.body) }) { player: Player ->
    player.flagAppearance()
}

fun needsUpdate(index: Int, parts: BodyParts): Boolean {
    val slot = EquipSlot.by(index)
    val part = BodyPart.by(slot) ?: return false
    return parts.updateConnected(part)
}
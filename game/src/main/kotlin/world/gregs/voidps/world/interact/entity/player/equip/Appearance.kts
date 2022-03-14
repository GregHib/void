import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.BodyPart
import world.gregs.voidps.network.visual.EquipSlot
import world.gregs.voidps.network.visual.update.player.Body

on<ItemChanged>({ container == "worn_equipment" && needsUpdate(index, it.body) }) { player: Player ->
    player.flagAppearance()
}

fun needsUpdate(index: Int, parts: Body): Boolean {
    val slot = EquipSlot.by(index)
    val part = BodyPart.by(slot) ?: return false
    return parts.updateConnected(part)
}
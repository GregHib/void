package world.gregs.voidps.engine.event.handle

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventProcessor
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Inventory(
    vararg val ids: String,
    val slots: Array<EquipSlot> = [],
    val inventory: String = "*",
)

object InventorySchema : EventProcessor.SchemaProvider {

    override fun param(param: ClassName): String {
        if (param.simpleName == Player::class.simpleName) {
            return "it"
        }
        return super.param(param)
    }

    private data object SlotSet : EventField {
        override fun get(data: Map<String, Any>): Set<Any> {
            val ids = data["slots"] as? List<KSType>
            return if (ids.isNullOrEmpty()) setOf("*") else ids.map { EquipSlot.by(it.toClassName().simpleName).index }.toSet()
        }
    }

    override fun schema(extension: String, params: List<ClassName>, data: Map<String, Any?>): List<EventField> = when (extension) {
        "ItemAdded" -> listOf(
            EventField.StaticValue("item_added"),
            EventField.StringList("ids"),
            SlotSet,
            EventField.StringKey("inventory"),
        )
        "ItemRemoved" -> listOf(
            EventField.StaticValue("item_removed"),
            EventField.StringList("ids"),
            SlotSet,
            EventField.StringKey("inventory"),
        )
        "InventorySlotChanged" -> listOf(
            EventField.StaticValue("inventory_changed"),
            SlotSet,
            EventField.StringKey("inventory"),
        )
        "InventoryUpdate" -> listOf(
            EventField.StaticValue("inventory_update"),
            EventField.StringKey("inventory"),
        )
        else -> emptyList()
    }

}
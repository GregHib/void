package world.gregs.voidps.engine.event.handle

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventProcessor

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Variable(
    vararg val ids: String,
    val npc: String = "*",
    val fromInt: Int = -1,
    val fromStr: String = "",
    val fromNull: Boolean = false,
    val fromBool: Boolean = false,
    val toInt: Int = -1,
    val toStr: String = "",
    val toNull: Boolean = false,
    val toBool: Boolean = false,
)

object VariableSchema : EventProcessor.SchemaProvider {

    private val set = setOf(Player::class.simpleName, NPC::class.simpleName)

    override fun param(param: ClassName): String {
        if (set.contains(param.simpleName)) {
            return "it"
        }
        return super.param(param)
    }

    override fun schema(extension: String, params: List<ClassName>, data: Map<String, Any?>): List<EventField> = when (extension) {
        "VariableSet" -> listOf(
            params.key("set_variable"),
            EventField.StringList("ids"),
            params.identifier(),
            EventField.StaticValue(value("from", data)),
            EventField.StaticValue(value("to", data)),
        )
        "VariableBitAdded" -> listOf(
            params.key("add_variable"),
            EventField.StringList("ids"),
            params.identifier(),
            EventField.StaticValue(value("to", data)),
        )
        "VariableBitRemoved" -> listOf(
            params.key("remove_variable"),
            EventField.StringList("ids"),
            params.identifier(),
            EventField.StaticValue(value("to", data)),
        )
        else -> emptyList()
    }

    private fun value(key: String, data: Map<String, Any?>): Any? {
        val int = data["${key}Int"] as? Int ?: -1
        if (int != -1) {
            return int
        }
        val string = data["${key}Str"] as? String ?: ""
        if (string != "") {
            return string
        }
        val bool = data["${key}Bool"] as? Boolean ?: false
        if (bool) {
            return true
        }
        val isNull = data["${key}Null"] as? Boolean ?: false
        if (isNull) {
            return null
        }
        return "*"
    }

}
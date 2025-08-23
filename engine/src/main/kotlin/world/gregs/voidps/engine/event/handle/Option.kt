package world.gregs.voidps.engine.event.handle

import com.squareup.kotlinpoet.ClassName
import world.gregs.voidps.engine.event.EventProcessor

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Option(
    val option: String = "*",
    vararg val ids: String,
    val approach: Boolean = false,
    val arrive: Boolean = true,
)

object OptionSchema : EventProcessor.SchemaProvider {

    override fun prefix(extension: String, data: Map<String, Any?>): String {
        if (extension == "FloorItemOption<Player>" || extension == "FloorItemOption<NPC>" || extension == "ObjectOption<Player>" || extension == "ObjectOption<NPC>") {
            if (data["arrive"] as Boolean) {
                return "arriveDelay()"
            }
        }
        return super.prefix(extension, data)
    }

    override fun param(param: ClassName): String {
        if (param.simpleName == "Player") {
            return "it"
        }
        return super.param(param)
    }

    override fun schema(extension: String, params: List<ClassName>) = when (extension) {
        "NPCOption<Player>" -> listOf(
            EventField.Event("player_operate_npc"),
            EventField.StringKey("option"),
            EventField.StringList("targets"),
        )
        "NPCOption<NPC>" -> listOf(
            EventField.Event("npc_operate_npc"),
            EventField.StringKey("option"),
            EventField.StringList("targets"),
        )
        "FloorItemOption<Player>" -> listOf(
            EventField.Event("player_operate_floor_item"),
            EventField.StringKey("option"),
            EventField.StringList("targets"),
            EventField.StaticString("player")
        )
        "FloorItemOption<NPC>" -> listOf(
            EventField.Event("npc_operate_floor_item"),
            EventField.StringKey("option"),
            EventField.StringList("targets"),
            EventField.StringList("ids"),
        )
        "ObjectOption<Player>" -> listOf(
            EventField.Event("player_operate_object"),
            EventField.StringKey("option"),
            EventField.StringList("ids"),
            EventField.StaticString("player")
        )
        "ObjectOption<NPC>" -> listOf(
            EventField.Event("npc_operate_object"),
            EventField.StringKey("option"),
            EventField.StringList("ids"),
            EventField.StringList("targets")
        )
        "PlayerOption<Player>" -> listOf(
            EventField.Event("player_operate_player"),
            EventField.StringKey("option"),
            EventField.StaticString("player")
        )
        "PlayerOption<NPC>" -> listOf(
            EventField.Event("npc_operate_player"),
            EventField.StringKey("option"),
            EventField.StringList("targets")
        )
        "InterfaceOption" -> listOf(
            EventField.Event("interface_option"),
            EventField.ListIndex("targets", 0),
            EventField.ListIndex("targets", 1),
            EventField.StringKey("option"),
            EventField.ListIndex("targets", 2)
        )
        else -> emptyList()
    }
}
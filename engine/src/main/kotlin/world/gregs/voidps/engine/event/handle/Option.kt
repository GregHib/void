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

    override fun schema(extension: String, params: List<ClassName>, data: Map<String, Any?>) = when (extension) {
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
            EventField.StaticValue("player")
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
            EventField.StaticValue("player")
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
            EventField.StaticValue("player")
        )
        "PlayerOption<NPC>" -> listOf(
            EventField.Event("npc_operate_player"),
            EventField.StringKey("option"),
            EventField.StringList("targets")
        )
        "InterfaceOption" -> listOf(
            EventField.StaticValue("interface_option"),
            EventField.ListIndex("targets", 0),
            EventField.ListIndex("targets", 1),
            EventField.StringKey("option"),
            EventField.ListIndex("targets", 2)
        )
        "InventoryOption" -> listOf(
            EventField.StaticValue("inventory_option"),
            EventField.StringKey("option"),
            EventField.SplitList("ids", 0),
            EventField.SplitList("ids", 1),
        )
        "ContinueDialogue" -> listOf(
            EventField.StaticValue("continue_dialogue"),
            EventField.SplitList("ids", 0),
            EventField.SplitList("ids", 1),
            EventField.StringKey("option"),
        )
        "ObjectTeleport" -> listOf(
            EventField {
                setOf("player_obj_teleport_${if (it["arrive"] as Boolean) "land" else "takeoff"}")
            },
            EventField.StringList("ids"),
            EventField.StringKey("option"),
        )
        else -> emptyList()
    }
}
package world.gregs.voidps.engine.event.handle

import com.squareup.kotlinpoet.ClassName
import world.gregs.voidps.engine.event.EventProcessor

/**
 * Interface/Item on Entities
 * @param use [interface, component] or items
 * @param on id of the entity
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class UseOn(
    val use: Array<String> = [],
    val on: Array<String> = [],
    val approach: Boolean = false,
    val arrive: Boolean = true,
)

object UseOnSchema : EventProcessor.SchemaProvider {
    override fun prefix(extension: String, data: Map<String, Any?>): String {
        if (extension == "InterfaceOnFloorItem" || extension == "ItemOnFloorItem" || extension == "InterfaceOnObject" || extension == "ItemOnObject") {
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
        "InterfaceOnFloorItem" -> listOf(
            EventField.Event("interface_operate_floor_item"),
            EventField.StringList("on"),
            EventField.SplitList("use", 0),
            EventField.SplitList("use", 1),
        )
        "InterfaceOnItem" -> listOf(
            EventField.Event("interface_on_item"),
            EventField.StringList("on"),
            EventField.SplitList("use", 0),
            EventField.SplitList("use", 1),
        )
        "InterfaceOnNPC" -> listOf(
            EventField.Event("interface_on_operate_npc"),
            EventField.StringList("on"),
            EventField.SplitList("use", 0),
            EventField.SplitList("use", 1),
        )
        "InterfaceOnObject" -> listOf(
            EventField.Event("interface_on_operate_object"),
            EventField.StringList("on"),
            EventField.SplitList("use", 0),
            EventField.SplitList("use", 1),
        )
        "InterfaceOnPlayer" -> listOf(
            EventField.Event("interface_on_operate_player"),
            EventField.SplitList("use", 0),
            EventField.SplitList("use", 1),
        )
        "ItemOnFloorItem" -> listOf(
            EventField.Event("item_operate_floor_item"),
            EventField.StringList("use"),
            EventField.StringList("on"),
        )
        "ItemOnItem" -> listOf(
            // TODO directional
            EventField.Event("item_on_item"),
            EventField.StringList("use"),
            EventField.StringList("on"),
        )
        "ItemOnNPC" -> listOf(
            EventField.Event("item_on_operate_npc"),
            EventField.StringList("use"),
            EventField.StringList("on"),
        )
        "ItemOnObject" -> listOf(
            EventField.Event("item_on_operate_object"),
            EventField.StringList("use"),
            EventField.StringList("on"),
        )
        "ItemOnPlayer" -> listOf(
            EventField.Event("item_on_operate_player"),
            EventField.StringList("use"),
        )
        else -> emptyList()
    }
}

/*

Interface
Opened
Closed
Refreshed
Clicked

Variable
Set
Added
Removed

Timers
@Start
@Stop
@Tick

Combat
Start
Stop
Reached
Attack
Damage
Interaction
Prepare
Swing
SpecialAttack
SpecialAttackDamage
SpecialAttackPrepare

Area
Entered
Exited
Moved

Hunt
FloorItem
NPC
Object

Chat messages
Quick
Private
PrivateQuick
Clan
ClanQuick

Spawn
Despawn

AiTick

Inventory
Update
Added
Removed
SlotChanged
DropItems
BoughtItem
SoldItem

Region
Retry
Clear
Load

Death
AfterDeath

DoorOpened
OpenShop
ObjectTeleport
Teleport

Destroyed
Destructible
Droppable
Dropped
Takeable
Taken
Consumable
Consume

Prayer
Start
Stop





    InterfaceOnX
    - no option
    + always interface or item

    InterfaceOnNPC
    id
    component
    npc
    approach

    InterfaceOnObject
    id
    component
    obj
    arrive
    approach

    InterfaceOnPlayer
    id
    component
    approach

    ItemOnNPC
    item
    npc
    approach

    ItemOnObject
    item
    obj
    arrive
    approach

    ItemOnPlayer
    item
    approach

    NPCOption<Player, NPC, Character>
    option
    npc(s)
    approach

    PlayerOption<Player, NPC, Character>
    option
    npc(s)
    approach

    FloorItemOption<Player, NPC, Character>
    option
    item
    npc
    arrive
    approach

    ObjectOption<Player, NPC, Character>
    option
    objects
    npc
    arrive
    approach


 */
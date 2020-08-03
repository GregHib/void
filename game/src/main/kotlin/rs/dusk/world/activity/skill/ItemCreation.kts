package rs.dusk.world.activity.skill

import rs.dusk.engine.client.variable.*
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.player.display.InterfaceInteraction

IntVariable(755, Variable.Type.VARC, defaultValue = -1).register("skill_creation_item_0")
IntVariable(756, Variable.Type.VARC, defaultValue = -1).register("skill_creation_item_1")
IntVariable(757, Variable.Type.VARC, defaultValue = -1).register("skill_creation_item_2")
IntVariable(758, Variable.Type.VARC, defaultValue = -1).register("skill_creation_item_3")
IntVariable(759, Variable.Type.VARC, defaultValue = -1).register("skill_creation_item_4")
IntVariable(760, Variable.Type.VARC, defaultValue = -1).register("skill_creation_item_5")
IntVariable(1139, Variable.Type.VARC, defaultValue = -1).register("skill_creation_item_6")
IntVariable(1140, Variable.Type.VARC, defaultValue = -1).register("skill_creation_item_7")
IntVariable(1141, Variable.Type.VARC, defaultValue = -1).register("skill_creation_item_8")
IntVariable(1142, Variable.Type.VARC, defaultValue = -1).register("skill_creation_item_9")

StringVariable(132, Variable.Type.VARCSTR).register("skill_creation_name_0")
StringVariable(133, Variable.Type.VARCSTR).register("skill_creation_name_1")
StringVariable(134, Variable.Type.VARCSTR).register("skill_creation_name_2")
StringVariable(135, Variable.Type.VARCSTR).register("skill_creation_name_3")
StringVariable(136, Variable.Type.VARCSTR).register("skill_creation_name_4")
StringVariable(137, Variable.Type.VARCSTR).register("skill_creation_name_5")
StringVariable(280, Variable.Type.VARCSTR).register("skill_creation_name_6")
StringVariable(281, Variable.Type.VARCSTR).register("skill_creation_name_7")
StringVariable(282, Variable.Type.VARCSTR).register("skill_creation_name_8")
StringVariable(283, Variable.Type.VARCSTR).register("skill_creation_name_9")

InterfaceInteraction where { name == "skill_creation_amount" && component == "create1" } then {
    player.setVar("skill_creation_amount", 1, refresh = false)
}

InterfaceInteraction where { name == "skill_creation_amount" && component == "create5" } then {
    player.setVar("skill_creation_amount", 5, refresh = false)
}

InterfaceInteraction where { name == "skill_creation_amount" && component == "create10" } then {
    player.setVar("skill_creation_amount", 10, refresh = false)
}

InterfaceInteraction where { name == "skill_creation_amount" && component == "create_max" } then {
    val max = player.getVar("skill_creation_maximum", 1)
    player.setVar("skill_creation_amount", max, refresh = false)
}

InterfaceInteraction where { name == "skill_creation_amount" && component == "increment" } then {
    var current = player.getVar("skill_creation_amount", 0)
    val maximum = player.getVar("skill_creation_maximum", 1)
    current++
    if(current > maximum) {
        current = maximum
    }
    player.setVar("skill_creation_amount", current)
}

InterfaceInteraction where { name == "skill_creation_amount" && component == "decrement" } then {
    var current = player.getVar("skill_creation_amount", 0)
    current--
    if(current < 0) {
        current = 0
    }
    player.setVar("skill_creation_amount", current)
}
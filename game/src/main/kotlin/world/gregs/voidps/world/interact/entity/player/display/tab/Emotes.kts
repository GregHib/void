package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.BitwiseVariable
import world.gregs.voidps.engine.client.variable.StringMapVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where

StringMapVariable(
    465, Variable.Type.VARP, true, mapOf(
        "locked" to 0,
        "unlocked" to 7
    )
).register("lost_tribe_emotes")

StringMapVariable(
    1085, Variable.Type.VARP, true, mapOf(
        "locked" to 0,
        "unlocked" to 249852
    )
).register("zombie_hand_emote")

BitwiseVariable(
    802, Variable.Type.VARP, true, values = listOf(
        "Flap",
        "Slap Head",
        "Idea",
        "Stomp"
    )
).register("stronghold_of_security_emotes")

BitwiseVariable(
    313, Variable.Type.VARP, true, values = listOf(
        "Glass Wall",
        "Glass Box",
        "Climb Rope",
        "Lean",
        "Scared",
        "Zombie Dance",
        "Zombie Walk",
        "Bunny-hop",
        "Skillcape",
        "Snowman Dance",
        "Air Guitar",
        "Safety First",
        "Explore",
        "Trick",
        "Give Thanks",
        "Freeze"
    )
).register("event_emotes")

InterfaceOpened where { name == "emotes" } then {
    player.interfaceOptions.unlockAll("emotes", "emotes", 0..190)
    player.sendVar("lost_tribe_emotes")
    player.sendVar("stronghold_of_security_emotes")
    player.sendVar("zombie_hand_emote")
    player.sendVar("event_emotes")
}
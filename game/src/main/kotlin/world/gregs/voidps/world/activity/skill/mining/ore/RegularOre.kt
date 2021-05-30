package world.gregs.voidps.world.activity.skill.mining.ore

enum class RegularOre : Ore {
    Clay,
    Rune_Essence,
    Copper_Ore,
    Tin_Ore,
    Limestone,
    Blurite,
    Iron,
    Silver,
    Pure_Essence,
    Coal,
    Sandstone,
    Gold,
    Granite,
    Mithril,
    Adamantite,
    Runite;

    override val id: String = name.toLowerCase()

}
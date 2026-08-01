package world.gregs.voidps.cache.definition

object Category {
    const val THROWABLE = 1
    const val ARROW = 2
    const val BOLT = 3
    const val CONSTRUCTION = 4
    const val FURNITURE = 5
    const val UNCOOKED_FOOD = 6
    const val CONSTRUCTION_STORABLE_CLOTHES = 7
    const val CRAFTING = 8
    const val SUMMONING_POUCHES = 9
    const val CONSTRUCTION_PLANT = 10
    const val FLETCHING = 11
    const val EDIBLE = 12
    const val HERBLORE = 13
    const val HUNTER_REQUIRED_ITEM = 14
    const val HUNTER_REWARD = 15
    const val JEWELLERY = 16
    const val MAGIC_ARMOUR = 17
    const val MAGIC_WEAPON = 18
    const val MELEE_ARMOUR_LOW = 19
    const val MELEE_ARMOUR_MID = 20
    const val MELEE_ARMOUR_HIGH = 21
    const val MELEE_WEAPON_LOW = 22
    const val MELEE_WEAPON_MID = 23
    const val MELEE_WEAPON_HIGH = 24
    const val MINING_SMELTING = 25
    const val POTION = 26
    const val PRAYER_ARMOUR = 27
    const val PRAYER_CONSUMABLE = 28
    const val RANGE_ARMOUR = 29
    const val RANGE_WEAPON = 30
    const val RUNECRAFTING = 31
    const val TELEPORT = 32
    const val SEED = 33
    const val SUMMONING_SCROLL = 34
    const val ITEM_ON_ITEM = 35
    const val LOG = 36

    fun name(id: Int) = when (id) {
        THROWABLE -> "throwable"
        ARROW -> "arrow"
        BOLT -> "bolt"
        CONSTRUCTION -> "construction"
        FURNITURE -> "furniture"
        UNCOOKED_FOOD -> "uncooked_food"
        CONSTRUCTION_STORABLE_CLOTHES -> "construction_storable_clothes"
        CRAFTING -> "crafting"
        SUMMONING_POUCHES -> "summoning_pouches"
        CONSTRUCTION_PLANT -> "construction_plant"
        FLETCHING -> "fletching"
        EDIBLE -> "edible"
        HERBLORE -> "herblore"
        HUNTER_REQUIRED_ITEM -> "hunter_required_item"
        HUNTER_REWARD -> "hunter_reward"
        JEWELLERY -> "jewellery"
        MAGIC_ARMOUR -> "magic_armour"
        MAGIC_WEAPON -> "magic_weapon"
        MELEE_ARMOUR_LOW -> "melee_armour_low"
        MELEE_ARMOUR_MID -> "melee_armour_mid"
        MELEE_ARMOUR_HIGH -> "melee_armour_high"
        MELEE_WEAPON_LOW -> "melee_weapon_low"
        MELEE_WEAPON_MID -> "melee_weapon_mid"
        MELEE_WEAPON_HIGH -> "melee_weapon_high"
        MINING_SMELTING -> "mining_smelting"
        POTION -> "potion"
        PRAYER_ARMOUR -> "prayer_armour"
        PRAYER_CONSUMABLE -> "prayer_consumable"
        RANGE_ARMOUR -> "range_armour"
        RANGE_WEAPON -> "range_weapon"
        RUNECRAFTING -> "runecrafting"
        TELEPORT -> "teleport"
        SEED -> "seed"
        SUMMONING_SCROLL -> "summoning_scroll"
        ITEM_ON_ITEM -> "item_on_item"
        LOG -> "log"
        else -> error("Category $id not found")
    }

    fun id(name: String) = when (name) {
        "throwable" -> THROWABLE
        "arrow" -> ARROW
        "bolt" -> BOLT
        "construction" -> CONSTRUCTION
        "furniture" -> FURNITURE
        "uncooked_food" -> UNCOOKED_FOOD
        "construction_storable_clothes" -> CONSTRUCTION_STORABLE_CLOTHES
        "crafting" -> CRAFTING
        "summoning_pouches" -> SUMMONING_POUCHES
        "construction_plant" -> CONSTRUCTION_PLANT
        "fletching" -> FLETCHING
        "edible" -> EDIBLE
        "herblore" -> HERBLORE
        "hunter_required_item" -> HUNTER_REQUIRED_ITEM
        "hunter_reward" -> HUNTER_REWARD
        "jewellery" -> JEWELLERY
        "magic_armour" -> MAGIC_ARMOUR
        "magic_weapon" -> MAGIC_WEAPON
        "melee_armour_low" -> MELEE_ARMOUR_LOW
        "melee_armour_mid" -> MELEE_ARMOUR_MID
        "melee_armour_high" -> MELEE_ARMOUR_HIGH
        "melee_weapon_low" -> MELEE_WEAPON_LOW
        "melee_weapon_mid" -> MELEE_WEAPON_MID
        "melee_weapon_high" -> MELEE_WEAPON_HIGH
        "mining_smelting" -> MINING_SMELTING
        "potion" -> POTION
        "prayer_armour" -> PRAYER_ARMOUR
        "prayer_consumable" -> PRAYER_CONSUMABLE
        "range_armour" -> RANGE_ARMOUR
        "range_weapon" -> RANGE_WEAPON
        "runecrafting" -> RUNECRAFTING
        "teleport" -> TELEPORT
        "seed" -> SEED
        "summoning_scroll" -> SUMMONING_SCROLL
        "item_on_item" -> ITEM_ON_ITEM
        "log" -> LOG
        else -> error("Category $name not found")
    }

}
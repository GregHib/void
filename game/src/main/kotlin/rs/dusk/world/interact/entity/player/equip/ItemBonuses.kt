package rs.dusk.world.interact.entity.player.equip

import rs.dusk.cache.definition.data.ItemDefinition

fun ItemDefinition.getInt(key: Long, default: Int): Int = params?.getOrDefault(key, default) as? Int ?: default

fun ItemDefinition.getString(key: Long, default: String): String = params?.getOrDefault(key, default) as? String ?: default

fun ItemDefinition.attackSpeed(): Int = getInt(14, 4)

fun ItemDefinition.has(key: Long): Boolean = params != null && params!!.containsKey(key)
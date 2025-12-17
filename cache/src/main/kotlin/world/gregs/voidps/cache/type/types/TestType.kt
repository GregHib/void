package world.gregs.voidps.cache.type.types

import world.gregs.voidps.cache.type.Type

data class TestType(
    val name: String = "null",
    val size: Int = 1,
    val options: List<String?> = listOf("Test", null, null),
    val list: List<String> = listOf("Test", "Test2"),
    val original: IntArray? = null,
//    val modified: IntArray? = null,
    val params: Map<String, Any>? = null,
) : Type {
    companion object {
        val EMPTY = TestType()
    }
}
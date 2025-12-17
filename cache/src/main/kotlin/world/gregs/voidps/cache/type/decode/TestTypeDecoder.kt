package world.gregs.voidps.cache.type.decode

import world.gregs.voidps.cache.type.TypeDecoder
import world.gregs.voidps.cache.type.types.TestType

class TestTypeDecoder : TypeDecoder<TestType>() {
    private val NAME = string("[section]", TestType.EMPTY.name, opcode = 1)
    private val SIZE = int("size", TestType.EMPTY.size, opcode = 4)
    private val OPTIONS = list("options", TestType.EMPTY.options, opcode = 3)
    private val LIST = list("list", TestType.EMPTY.list, opcode = 5)
    private val NUMBERS = intArray("numbers", TestType.EMPTY.original, opcode = 2)
    private val PARAMS = params(opcode = 249) {
        add("category", 1234)
        add("ranged_strength", 643)
        add("farming_xp", 1342)
        convert(
            "ranged_strength", "magic_strength",
            decode = { (it as Int) / 10.0 },
            encode = { (it as Double * 10.0).toInt() },
        )
        convertBinary("farming_xp",
            decode = { it as Int / 10.0 },
            encode = { (it as Double * 10.0).toInt()},
        )
    }

    override fun create() = TestType(
        name = NAME.value,
        size = SIZE.value,
        options = OPTIONS.value,
        list = LIST.value,
        original = NUMBERS.value,
        params = PARAMS.value,
    )

}
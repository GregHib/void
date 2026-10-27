package world.gregs.voidps.cache.definition.data

import kotlinx.serialization.Serializable
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.serialization.AnyArraySerializer
import world.gregs.voidps.cache.serialization.NullableStringArraySerializer
import world.gregs.voidps.cache.serialization.ParamsSerializer

@Serializable
data class InterfaceComponentDefinition(
    override var id: Int = -1,
    @Serializable(with = NullableStringArraySerializer::class)
    var options: Array<String?>? = null,
    @Serializable(with = AnyArraySerializer::class)
    var information: Array<Any>? = null,
    var baseX: Int = 0,
    var baseY: Int = 0,
    var defaultImage: Int = -1,
    override var stringId: String = "",
    @Serializable(with = ParamsSerializer::class)
    override var params: Map<Int, Any>? = null,
) : Definition,
    Parameterized {

    val parent: Int
        get() = InterfaceDefinition.id(id)

    val index: Int
        get() = InterfaceDefinition.componentId(id)

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (options?.contentHashCode() ?: 0)
        result = 31 * result + (information?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (params?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterfaceComponentDefinition

        if (id != other.id) return false
        if (defaultImage != other.defaultImage) return false
        if (!options.contentEquals(other.options)) return false
        if (!information.contentEquals(other.information)) return false
        if (stringId != other.stringId) return false
        if (params != other.params) return false
        if (parent != other.parent) return false
        if (index != other.index) return false

        return true
    }
}

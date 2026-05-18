package world.gregs.voidps.tools.search

import world.gregs.voidps.tools.search.screen.view.detail.ParamLookup
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.kotlinProperty


@Suppress("UNCHECKED_CAST")
fun <T : Any> getProperties(clazz: Class<T>): List<KProperty1<T, *>> {
    val companionProperties = clazz.kotlin.companionObject?.declaredMemberProperties?.toSet() ?: emptySet()
    return clazz.declaredFields
        .mapNotNull { it.kotlinProperty as? KProperty1<T, *> }
        .filter { !companionProperties.contains(it) }
}

fun propertyTypeLabel(prop: KProperty1<*, *>): String =
    prop.returnType.toString()
        .replace("kotlin.", "").replace("?", "")
        .substringAfterLast('.')

fun displayValue(value: Any?, resolveParams: Boolean = false): String = when (value) {
    null -> "null"
    is Array<*> -> value.joinToString(", ") { it?.toString() ?: "null" }
    is ByteArray -> value.joinToString(", ")
    is IntArray -> value.joinToString(", ")
    is ShortArray -> value.joinToString(", ")
    is Map<*, *> -> if (resolveParams) {
        value.entries.joinToString(", ") { (k, v) ->
            val name = (k as? Int)?.let { ParamLookup.of(it) } ?: k.toString()
            "$name=$v"
        }
    } else value.entries.joinToString(", ") { "${it.key}=${it.value}" }
    else -> value.toString()
}

fun copyToClipboard(text: String) =
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
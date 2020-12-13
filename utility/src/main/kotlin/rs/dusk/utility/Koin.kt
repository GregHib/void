package rs.dusk.utility

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.ext.getFloatProperty
import org.koin.ext.getIntProperty
import org.koin.java.KoinJavaComponent.getKoin

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */

inline fun <reified T : Any> get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T = getKoin().get(qualifier, parameters)

fun getIntProperty(key: String): Int = getKoin().getIntProperty(key)!!

fun getProperty(key: String): String = getKoin().getProperty(key)!!

fun getFloatProperty(key: String): Float = getKoin().getFloatProperty(key)!!

fun getIntProperty(key: String, defaultValue: Int): Int = getKoin().getIntProperty(key, defaultValue)

inline fun <reified T : Any> inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> = getKoin().inject(qualifier, parameters = parameters)

inline fun <reified S, reified P> bind(
    noinline parameters: ParametersDefinition? = null
): S = getKoin().bind<S, P>(parameters)
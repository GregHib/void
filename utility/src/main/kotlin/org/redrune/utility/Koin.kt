package org.redrune.utility

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.java.KoinJavaComponent.getKoin

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 26, 2020
 */

inline fun <reified T> get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T = getKoin().get(qualifier, parameters)

fun <T> getProperty(key: String): T? = getKoin().getProperty(key)

fun <T> getProperty(key: String, defaultValue: T): T = getKoin().getProperty(key, defaultValue)

inline fun <reified T> inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> = getKoin().inject()

inline fun <reified S, reified P> bind(
    noinline parameters: ParametersDefinition? = null
): S = getKoin().bind<S, P>(parameters)
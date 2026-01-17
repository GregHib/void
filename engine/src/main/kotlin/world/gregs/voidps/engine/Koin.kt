package world.gregs.voidps.engine

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.java.KoinJavaComponent.getKoin
import kotlin.reflect.KClass

inline fun <reified T : Any> get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = getKoin().get(qualifier, parameters)

inline fun <T : Any> get(
    kClass: KClass<T>,
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = getKoin().get(kClass, qualifier, parameters)

@Deprecated("Use script constructors instead")
inline fun <reified T : Any> inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): Lazy<T> = getKoin().inject(qualifier, parameters = parameters)

package com.davismiyashiro.weathermapapp.injection

import com.airbnb.mvrx.MavericksViewModel
import dagger.MapKey
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@MapKey
annotation class ViewModelKey(val value: KClass<out MavericksViewModel<*>>)
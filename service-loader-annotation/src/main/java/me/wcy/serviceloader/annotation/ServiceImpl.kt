package me.wcy.serviceloader.annotation

import kotlin.reflect.KClass

/**
 * Created by wangchenyan.top on 2022/8/10.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceImpl(
    val value: KClass<*>
)

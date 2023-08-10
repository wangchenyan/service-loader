package me.wcy.serviceloader.api

import kotlin.reflect.KClass

/**
 * Created by wangchenyan.top on 2023/8/10.
 */
data class ServiceImplEntity(
    val implClass: KClass<*>,
    val singleton: Boolean
)

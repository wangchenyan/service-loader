package me.wcy.serviceloader.annotation

import kotlin.reflect.KClass

/**
 * Created by wangchenyan.top on 2023/6/17.
 */
data class ServiceImplEntity(
    val implClass: KClass<*>,
    val singleton: Boolean
)

package me.wcy.serviceloader.app

import me.wcy.apple.api.INonSingleton
import me.wcy.serviceloader.annotation.ServiceImpl

/**
 * Created by wangchenyan.top on 2023/6/17.
 */
@ServiceImpl(INonSingleton::class, singleton = false)
class NonSingletonImpl : INonSingleton {
}
package me.wcy.banana

import me.wcy.apple.api.IApple
import me.wcy.serviceloader.annotation.ServiceImpl

/**
 * Created by wangchenyan.top on 2022/8/11.
 */
@ServiceImpl(IApple::class)
class BananaAppleImpl : IApple {
    override fun getName(): String {
        return "BananaApple"
    }
}
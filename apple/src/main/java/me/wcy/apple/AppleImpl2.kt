package me.wcy.apple

import me.wcy.apple.api.IApple
import me.wcy.serviceloader.annotation.ServiceImpl

/**
 * Created by wangchenyan.top on 2022/8/10.
 */
@ServiceImpl(IApple::class)
class AppleImpl2 : IApple {
    override fun getName(): String {
        return "Apple2"
    }
}
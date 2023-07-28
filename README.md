# service-loader

[![](https://jitpack.io/v/wangchenyan/service-loader.svg)](https://jitpack.io/#wangchenyan/service-loader)

Android 支持组件化的服务发现框架

## Feature

- 适用于组件化项目
- 服务发现能力
- 支持单例模式和非单例模式

## Usage

### 1. 添加 Jitpack 仓库

```kotlin
// settings build file
pluginManagement {
    repositories {
        maven("https://jitpack.io")
    }
}
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}
```

### 2. 添加 auto-register 插件，用于字节码注入

```kotlin
// root build file
buildscript {
    dependencies {
        classpath("com.github.wangchenyan:AutoRegister:1.4.3-beta02")
    }
}
```

```kotlin
// app build file
plugins {
    id("auto-register")
}

autoregister {
    registerInfo = listOf(
        mapOf(
            "scanInterface" to "me.wcy.serviceloader.annotation.IServiceLoader",
            "codeInsertToClassName" to "me.wcy.serviceloader.api.ServiceLoader",
            "codeInsertToMethodName" to "init",
            "registerMethodName" to "register",
            "include" to listOf("me/wcy/serviceloader/apt/.*")
        )
    )
    // 如果 ASM 版本不兼容，可修改为兼容版本，默认为 ASM6
    amsApiVersion = Opcodes.ASM6
}
```

### 3. 添加 service loader 依赖和注解处理器

```kotlin
// app build file
plugins {
    id("kotlin-kapt")
}

kapt {
    arguments {
        arg("moduleName", project.name)
    }
}

dependencies {
    kapt("com.github.wangchenyan.service-loader:service-loader-compiler:${latestVersion}")
    implementation("com.github.wangchenyan.service-loader:service-loader-api:${latestVersion}")
}
```

### 4. 在代码中使用

#### 4.1 定义接口

```kotlin
interface IApple {
    fun getName(): String
}
```

#### 4.2 定义接口实现

实现类需要添加 `@ServiceImpl` 注解，参数

- singleton: true，表示单例模式，每次获取都是同一个实例，否则，表示非单例模式，每次获取都返回新的实例

```kotlin
@ServiceImpl(IApple::class, singleton = true)
class AppleImpl : IApple {
    override fun getName(): String {
        return "Apple1"
    }
}
```

> 注意：接口和实现需要定义在不同 module 中。

#### 4.3 发现服务

```kotlin
// 获取全部实例
ServiceLoader.loadAll(IApple::class)

// 获取一个实例
ServiceLoader.loadFirstOrNull(IApple::class)
```

## About Me

掘金：https://juejin.cn/user/2313028193754168<br>
微博：https://weibo.com/wangchenyan1993

## License

    Copyright 2023 wangchenyan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

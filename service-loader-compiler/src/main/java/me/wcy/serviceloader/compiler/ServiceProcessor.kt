package me.wcy.serviceloader.compiler

import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import me.wcy.serviceloader.annotation.IServiceLoader
import me.wcy.serviceloader.annotation.ServiceImpl
import me.wcy.serviceloader.annotation.ServiceImplInfo

/**
 * Created by wangchenyan.top on 2022/8/10.
 */
class ServiceProcessor : SymbolProcessor, SymbolProcessorProvider {
    private lateinit var codeGenerator: CodeGenerator
    private lateinit var moduleName: String

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        Log.setLogger(environment.logger)
        codeGenerator = environment.codeGenerator

        val moduleName = environment.options["moduleName"]
        if (moduleName.isNullOrEmpty()) {
            Log.exception(
                TAG,
                "Can not find ksp argument 'moduleName', check if has add the code like this in module's build.gradle.kts:\n" +
                        "\n" +
                        "    ksp {\n" +
                        "        arg(\"moduleName\", project.name)\n" +
                        "    }" +
                        "\n"
            )
        }

        this.moduleName = ProcessorUtils.formatModuleName(moduleName!!)

        Log.w(TAG, "Start to deal module ${this.moduleName}")
        return this
    }

    @OptIn(KspExperimental::class, DelicateKotlinPoetApi::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val serviceImplList =
            resolver.getSymbolsWithAnnotation(ServiceImpl::class.java.name).toList()
        if (serviceImplList.isEmpty()) {
            return emptyList()
        }

        val serviceMap = mutableMapOf<String, MutableList<Pair<String, Boolean>>>()

        serviceImplList.forEach {
            checkDeclaration(it)
            val declaration = it as KSDeclaration
            val anno = declaration.getAnnotationsByType(ServiceImpl::class).first()
            val serviceName = anno.getServiceName()
            val implName = it.toClassName().canonicalName

            Log.w(TAG, "Collected service: $implName -> $serviceName")

            val list = serviceMap[serviceName] ?: kotlin.run {
                serviceMap[serviceName] = mutableListOf()
                serviceMap[serviceName]!!
            }
            list.add(Pair(implName, anno.singleton))
        }

        if (serviceMap.isEmpty()) {
            return emptyList()
        }

        /**
         * Param type: ServiceImplInfo
         */
        val entityTypeName = ServiceImplInfo::class.asTypeName()

        /**
         * Param type: List<ServiceImplInfo>
         */
        val setTypeName = List::class.asTypeName().parameterizedBy(entityTypeName)

        /**
         * Param type: MutableMap<String, List<ServiceImplInfo>>
         */
        val mapTypeName = ClassName(
            "kotlin.collections",
            "MutableMap"
        ).parameterizedBy(String::class.asTypeName(), setTypeName)

        /**
         * Param name: map: MutableMap<String, List<ServiceImplInfo>>
         *
         * There's no such type as MutableMap at runtime so the library only sees the runtime type.
         * If you need MutableMap then you'll need to use a ClassName to create it.
         * [https://github.com/square/kotlinpoet/issues/482]
         */
        val mapParamSpec =
            ParameterSpec.builder(ProcessorUtils.PARAM_MAP_NAME, mapTypeName).build()

        /**
         * Method: override fun load(map: MutableMap<String, List<ServiceImplInfo>>)
         */
        val loadTaskMethodBuilder = FunSpec.builder(ProcessorUtils.METHOD_LOAD_NAME)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(mapParamSpec)

        serviceMap.forEach { (serviceName, implList) ->
            /**
             * Statement: map.put(serviceName, listOf(ServiceImplInfo(serviceImplName, true)))
             */
            val format = StringBuilder()
            val args: MutableList<Any> = mutableListOf(ProcessorUtils.PARAM_MAP_NAME, serviceName)
            implList.forEach {
                format.append("%T(%S, %L),\n")
                args.addAll(listOf(ServiceImplInfo::class, it.first, it.second))
            }
            if (format.isNotEmpty()) {
                format.deleteAt(format.length - 2)
            }

            loadTaskMethodBuilder.addStatement(
                "%N.put(%S, listOf(\n$format))",
                *args.toTypedArray()
            )
        }

        /**
         * Write to file
         */
        val fileSpec = FileSpec.builder(ProcessorUtils.PACKAGE_NAME, "ServiceLoader\$$moduleName")
            .addType(
                TypeSpec.classBuilder("ServiceLoader\$$moduleName")
                    .addKdoc(ProcessorUtils.JAVADOC)
                    .addSuperinterface(IServiceLoader::class.java)
                    .addFunction(loadTaskMethodBuilder.build())
                    .build()
            )
            .build()

        val file =
            codeGenerator.createNewFile(Dependencies.ALL_FILES, fileSpec.packageName, fileSpec.name)
        file.write(fileSpec.toString().toByteArray())

        return emptyList()
    }

    /**
     * 检查注解是否合法
     * 1. 注解类为 Class 类型
     * 2. 注解类实现注解中的接口
     */
    @OptIn(KspExperimental::class)
    private fun checkDeclaration(annotated: KSAnnotated) {
        check(annotated is KSClassDeclaration) {
            "Type [${annotated}] with annotation [${ServiceImpl::class.java.name}] should be a class"
        }
        val anno = annotated.getAnnotationsByType(ServiceImpl::class).first()
        val serviceName = anno.getServiceName()
        checkNotNull(annotated.getAllSuperTypes().find {
            it.declaration.toClassName().canonicalName == serviceName
        }) {
            "Type [${annotated.toClassName().canonicalName}] with annotation [${ServiceImpl::class.java.name}] should implements [$serviceName]"
        }
    }

    @OptIn(KspExperimental::class)
    private fun ServiceImpl.getServiceName(): String {
        return try {
            value.qualifiedName ?: ""
        } catch (e: KSTypeNotPresentException) {
            e.ksType.declaration.qualifiedName!!.asString()
        }
    }

    companion object {
        private const val TAG = "ServiceLoader"
    }
}
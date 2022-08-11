package me.wcy.serviceloader.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asTypeName
import me.wcy.serviceloader.annotation.IServiceLoader
import me.wcy.serviceloader.annotation.ServiceImpl
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.reflect.KClass

/**
 * Created by wangchenyan.top on 2022/8/10.
 */
class ServiceProcessor : AbstractProcessor() {
    private lateinit var filer: Filer
    private lateinit var elementUtil: Elements
    private lateinit var typeUtil: Types
    private lateinit var moduleName: String

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        elementUtil = processingEnv.elementUtils
        typeUtil = processingEnv.typeUtils
        Log.setTag(TAG)
        Log.setLogger(processingEnv.messager)

        val moduleName = processingEnv.options["moduleName"]
        if (moduleName == null || moduleName.isEmpty()) {
            Log.exception(
                "Can not find apt argument 'moduleName', check if has add the code like this in module's build.gradle:\n" +
                        "    In Kotlin:\n" +
                        "    \n" +
                        "    kapt {\n" +
                        "        arguments {\n" +
                        "          arg(\"moduleName\", project.name)\n" +
                        "        }\n" +
                        "    }\n"
            )
        }

        this.moduleName = ProcessorUtils.formatModuleName(moduleName!!)

        Log.i("Start to deal module ${this.moduleName}")
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        val supportAnnotationTypes = mutableSetOf<String>()
        supportAnnotationTypes.add(ServiceImpl::class.java.canonicalName)
        return supportAnnotationTypes
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    @OptIn(DelicateKotlinPoetApi::class)
    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        val taskElements = roundEnv.getElementsAnnotatedWith(ServiceImpl::class.java)
        if (taskElements == null || taskElements.size == 0) {
            return false
        }

        val serviceMap = mutableMapOf<KClass<*>, MutableList<TypeMirror>>()

        taskElements.forEach { element ->
            val typeElement = (element as? TypeElement) ?: return@forEach
            val anno = typeElement.getAnnotation(ServiceImpl::class.java)
            val service = try {
                anno.value
            } catch (e: MirroredTypeException) {
                val className = e.typeMirror.asTypeName().toString()
                Class.forName(className).kotlin
            }
            val serviceType = elementUtil.getTypeElement(service.qualifiedName).asType()
            val implType = typeElement.asType()
            if (typeUtil.isSubtype(implType, serviceType).not()) {
                Log.exception("Impl $implType is not a Subtype of Service $serviceType")
            }
            Log.i("Collected Service: ${typeElement.qualifiedName} -> ${service.qualifiedName}")
            val list = serviceMap[service] ?: kotlin.run {
                serviceMap[service] = mutableListOf()
                serviceMap[service]!!
            }
            list.add(typeElement.asType())
        }

        if (serviceMap.isEmpty()) {
            return false
        }

        /**
         * Param type: KClass<out Any>
         */
        val kClassTypeName =
            KClass::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(Any::class))

        /**
         * Param type: List<KClass<out Any>>
         */
        val setTypeName = List::class.asTypeName().parameterizedBy(kClassTypeName)

        /**
         * Param type: MutableMap<KClass<out Any>, List<KClass<out Any>>>
         */
        val mapTypeName = ClassName(
            "kotlin.collections",
            "MutableMap"
        ).parameterizedBy(kClassTypeName, setTypeName)

        /**
         * Param name: map: MutableMap<KClass<out Any>, List<KClass<out Any>>>
         *
         * There's no such type as MutableList at runtime so the library only sees the runtime type.
         * If you need MutableList then you'll need to use a ClassName to create it.
         * [https://github.com/square/kotlinpoet/issues/482]
         */
        val mapParamSpec =
            ParameterSpec.builder(ProcessorUtils.PARAM_MAP_NAME, mapTypeName).build()

        /**
         * Method: override fun load(map: MutableMap<KClass<out Any>, List<KClass<out Any>>>)
         */
        val loadTaskMethodBuilder = FunSpec.builder(ProcessorUtils.METHOD_LOAD_NAME)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(mapParamSpec)

        serviceMap.forEach { (service, implList) ->
            /**
             * Statement: map.put(Service::class, listOf(ServiceImpl::class))
             */
            val format = StringBuilder()
            val args: MutableList<Any> = mutableListOf(ProcessorUtils.PARAM_MAP_NAME, service.java)
            implList.forEach {
                format.append("%T::class,")
                args.add(it)
            }
            if (format.isNotEmpty()) {
                format.deleteAt(format.length - 1)
            }

            loadTaskMethodBuilder.addStatement(
                "%N.put(%T::class, listOf($format))",
                *args.toTypedArray()
            )
        }

        /**
         * Write to file
         */
        FileSpec.builder(ProcessorUtils.PACKAGE_NAME, "ServiceLoader\$$moduleName")
            .addType(
                TypeSpec.classBuilder("ServiceLoader\$$moduleName")
                    .addKdoc(ProcessorUtils.JAVADOC)
                    .addSuperinterface(IServiceLoader::class.java)
                    .addFunction(loadTaskMethodBuilder.build())
                    .build()
            )
            .build()
            .writeTo(filer)

        return true
    }

    companion object {
        private const val TAG = "ServiceLoader"
    }
}
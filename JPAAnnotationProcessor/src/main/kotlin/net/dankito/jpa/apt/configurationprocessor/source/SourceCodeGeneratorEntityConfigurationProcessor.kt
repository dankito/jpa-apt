package net.dankito.jpa.apt.configurationprocessor.source

import com.squareup.javapoet.*
import net.dankito.jpa.apt.config.EntityConfig
import net.dankito.jpa.apt.config.JPAEntityConfiguration
import net.dankito.jpa.apt.configurationprocessor.IEntityConfigurationProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.persistence.AccessType


class SourceCodeGeneratorEntityConfigurationProcessor : IEntityConfigurationProcessor {

    override fun processConfiguration(entityConfiguration: JPAEntityConfiguration, processingEnv: ProcessingEnvironment) {
        val createdEntityConfigs = createEntityConfigClasses(entityConfiguration, processingEnv)

        createEntityConfigClassesLoader(createdEntityConfigs, processingEnv)
    }

    private fun createEntityConfigClasses(entityConfiguration: JPAEntityConfiguration, processingEnv: ProcessingEnvironment): List<ClassName> {
        return entityConfiguration.entities.map { createEntityConfigClass(it, processingEnv) }
    }

    private fun createEntityConfigClass(entityConfig: EntityConfig, processingEnv: ProcessingEnvironment) : ClassName {
        val className = (entityConfig.tableName.substring(0, 1).toUpperCase() + entityConfig.tableName.substring(1)) + "EntityConfig"
        val packageName = entityConfig.entityClass.`package`.name

        val entityClassName = ClassName.get(entityConfig.entityClass)

        val constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super(\$T.class)", entityClassName)
                .addStatement("this.\$N = \$S", "tableName", entityConfig.tableName)

        if(entityConfig.access == null) {
            constructorBuilder.addStatement("this.setAccess(null)")
        }
        else {
            constructorBuilder.addStatement("this.setAccess(\$T.\$L)", ClassName.get(AccessType::class.java), entityConfig.access.toString())
        }
        val constructor = constructorBuilder.build()

        val entityClass = TypeSpec.classBuilder(className)
                .superclass(EntityConfig::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .build()

        val javaFile = JavaFile.builder(packageName, entityClass)
                .build()

        javaFile.writeTo(processingEnv.filer)

        return ClassName.get(packageName, className)
    }


    private fun createEntityConfigClassesLoader(createdEntityConfigs: List<ClassName>, processingEnv: ProcessingEnvironment) {
        val className = "GeneratedEntityConfigs"

        val entityConfig = ClassName.get(EntityConfig::class.java)
        val list = ClassName.get("java.util", "List")
        val arrayList = ClassName.get("java.util", "ArrayList")
        val listOfEntityConfigs = ParameterizedTypeName.get(list, entityConfig)

        val getGeneratedEntityConfigsBuilder = MethodSpec.methodBuilder("getGeneratedEntityConfigs")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfEntityConfigs)
                .addStatement("\$T result = new \$T<>()", listOfEntityConfigs, arrayList)

        for(createdEntityConfig in createdEntityConfigs) {
            getGeneratedEntityConfigsBuilder.addStatement("result.add(new \$T())", createdEntityConfig)
        }

        getGeneratedEntityConfigsBuilder.addStatement("return result")

        val getGeneratedEntityConfigs = getGeneratedEntityConfigsBuilder.build()


        val entityClass = TypeSpec.classBuilder(className)
                .superclass(EntityConfig::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(getGeneratedEntityConfigs)
                .build()

        val javaFile = JavaFile.builder("", entityClass)
                .build()

        javaFile.writeTo(processingEnv.filer)
    }
}
package net.dankito.jpa.apt.configurationprocessor

import net.dankito.jpa.apt.config.JPAEntityConfiguration
import javax.annotation.processing.ProcessingEnvironment


interface IEntityConfigurationProcessor {

    fun processConfiguration(entityConfiguration: JPAEntityConfiguration, processingEnv: ProcessingEnvironment)

}
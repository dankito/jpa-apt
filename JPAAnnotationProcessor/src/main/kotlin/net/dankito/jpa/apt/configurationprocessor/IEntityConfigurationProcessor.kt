package net.dankito.jpa.apt.configurationprocessor

import net.dankito.jpa.apt.config.JpaEntityConfiguration
import javax.annotation.processing.ProcessingEnvironment


interface IEntityConfigurationProcessor {

    fun processConfiguration(entityConfiguration: JpaEntityConfiguration, processingEnv: ProcessingEnvironment)

}
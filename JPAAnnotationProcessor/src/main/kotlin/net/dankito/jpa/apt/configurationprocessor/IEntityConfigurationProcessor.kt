package net.dankito.jpa.apt.configurationprocessor

import net.dankito.jpa.apt.config.JpaEntityConfiguration


interface IEntityConfigurationProcessor {

    fun processConfiguration(entityConfiguration: JpaEntityConfiguration)

}
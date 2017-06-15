package net.dankito.jpa.apt.serializer

import net.dankito.jpa.apt.config.JpaEntityConfiguration
import java.io.InputStream
import java.net.URL


interface IJPAEntityConfigurationSerializer {

    fun serializeJPAEntityConfiguration(entityConfiguration: JpaEntityConfiguration): String


    fun deserializeJPAEntityConfiguration(serializedEntityConfiguration: String): JpaEntityConfiguration

    fun deserializeJPAEntityConfiguration(serializedEntityConfigurationUrl: URL): JpaEntityConfiguration

    fun deserializeJPAEntityConfiguration(inputStream: InputStream): JpaEntityConfiguration

}
package net.dankito.jpa.apt.serializer

import net.dankito.jpa.apt.config.JPAEntityConfiguration
import java.io.InputStream
import java.net.URL


interface IJPAEntityConfigurationSerializer {

    fun serializeJPAEntityConfiguration(entityConfiguration: JPAEntityConfiguration): String


    fun deserializeJPAEntityConfiguration(serializedEntityConfiguration: String): JPAEntityConfiguration

    fun deserializeJPAEntityConfiguration(serializedEntityConfigurationUrl: URL): JPAEntityConfiguration

    fun deserializeJPAEntityConfiguration(inputStream: InputStream): JPAEntityConfiguration

}
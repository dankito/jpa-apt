package net.dankito.jpa.apt.configurationprocessor.json.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.lang.reflect.Member


abstract class ReflectionClassesSerializerBase<T: Member>(clazz: Class<T>) : StdSerializer<T>(clazz) {

    override fun serialize(value: T?, gen: JsonGenerator?, provider: SerializerProvider?) {
        value?.let { value ->
            gen?.let { jsonGenerator ->
                jsonGenerator.writeStartObject()

                jsonGenerator.writeStringField(SerializerConfig.ClassNameFieldName, value.declaringClass.name)
                writeInstanceIdentifier(jsonGenerator, value)

                jsonGenerator.writeEndObject()
            }
        }
    }

    abstract fun writeInstanceIdentifier(jsonGenerator: JsonGenerator, value: T)

}
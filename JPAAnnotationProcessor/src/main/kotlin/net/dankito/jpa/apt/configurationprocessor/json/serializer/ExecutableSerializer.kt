package net.dankito.jpa.apt.configurationprocessor.json.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.lang.reflect.Executable


class ExecutableSerializer : StdSerializer<Executable>(Executable::class.java) {

    override fun serialize(value: Executable?, gen: JsonGenerator?, provider: SerializerProvider?) {
        value?.let { executable ->
            gen?.let { jsonGenerator ->
                jsonGenerator.writeStartObject()

                jsonGenerator.writeStringField(SerializerConfig.ClassNameFieldName, executable.declaringClass.name)
                jsonGenerator.writeStringField(SerializerConfig.MethodNameFieldName, executable.name)

                jsonGenerator.writeEndObject()
            }
        }
    }

}
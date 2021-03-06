package net.dankito.jpa.apt.serializer.json

import com.fasterxml.jackson.core.JsonGenerator
import java.lang.reflect.Field


class FieldSerializer : ReflectionClassesSerializerBase<Field>(Field::class.java) {

    override fun writeInstanceIdentifier(jsonGenerator: JsonGenerator, value: Field) {
        jsonGenerator.writeStringField(SerializerConfig.FieldNameFieldName, value.name)
    }

}
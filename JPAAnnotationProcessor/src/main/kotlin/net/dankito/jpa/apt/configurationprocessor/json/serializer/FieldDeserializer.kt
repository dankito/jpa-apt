package net.dankito.jpa.apt.configurationprocessor.json.serializer

import com.fasterxml.jackson.databind.node.ObjectNode
import java.lang.reflect.Field


class FieldDeserializer : ReflectionClassesDeserializerBase<Field>(Field::class.java) {

    override fun instantiateObject(declaringClass: Class<*>, node: ObjectNode): Field {
        val fieldName = node.get(SerializerConfig.FieldNameFieldName).asText()
        return declaringClass.getDeclaredField(fieldName)
    }

}
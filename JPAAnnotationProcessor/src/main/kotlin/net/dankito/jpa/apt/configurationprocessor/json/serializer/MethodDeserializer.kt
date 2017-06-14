package net.dankito.jpa.apt.configurationprocessor.json.serializer

import java.lang.reflect.Method


class MethodDeserializer : com.fasterxml.jackson.databind.deser.std.StdDeserializer<Method>(java.lang.reflect.Method::class.java) {

    @Throws(com.fasterxml.jackson.core.JsonProcessingException::class, java.io.IOException::class)
    override fun deserialize(p: com.fasterxml.jackson.core.JsonParser?, ctxt: com.fasterxml.jackson.databind.DeserializationContext?): java.lang.reflect.Method {
        p?.let { jsonParser ->
            try {
                return deserializeMethodNode(jsonParser)
            } catch(e: Exception) {
                throw java.io.IOException("Could not deserialize Method", e)
            }
        }

        throw java.io.IOException("Could not deserialize method, JsonParser is null")
    }

    private fun deserializeMethodNode(jsonParser: com.fasterxml.jackson.core.JsonParser): java.lang.reflect.Method {
        val node = jsonParser.getCodec().readTree<com.fasterxml.jackson.databind.node.ObjectNode>(jsonParser)

        val className = node.get(SerializerConfig.Companion.ClassNameFieldName).asText()
        val methodName = node.get(SerializerConfig.Companion.MethodNameFieldName).asText()

        try {
            val declaringClass = Class.forName(className)
            return declaringClass.getDeclaredMethod(methodName)
        } catch(e: Exception) {
            throw java.io.IOException("Could not deserialize Method from class name \'$className\' and method name \'$methodName\'", e)
        }
    }

}
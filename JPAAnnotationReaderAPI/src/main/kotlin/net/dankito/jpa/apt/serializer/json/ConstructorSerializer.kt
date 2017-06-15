package net.dankito.jpa.apt.serializer.json

import com.fasterxml.jackson.core.JsonGenerator
import java.lang.reflect.Constructor


class ConstructorSerializer : ReflectionClassesSerializerBase<Constructor<*>>(Constructor::class.java) {

    override fun writeInstanceIdentifier(jsonGenerator: JsonGenerator, value: Constructor<*>) {
        // as currently only no-arg constructors are supported, nothing to do in this case
    }

}
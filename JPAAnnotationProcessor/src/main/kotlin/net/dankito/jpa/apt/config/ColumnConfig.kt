package net.dankito.jpa.apt.config

import javax.persistence.CascadeType
import javax.persistence.FetchType
import javax.persistence.GenerationType


class ColumnConfig(val entityConfig: EntityConfig<*>, val property: Property) {

    var type: Class<*> = property.field.type

    var columnName = property.field.name // default value, may be overwritten by @Column (or other) annotation
    var tableName: String? = null

    var isId: Boolean = false
    var isGeneratedId: Boolean = false
    var generatedIdType = GenerationType.AUTO
    var idGenerator: String? = null
    var generatedIdSequence: String? = null

    var isVersion = false

    var isLob = false

    var columnDefinition: String? = null
    var length = 255
    var scale = 0
    var precision = 0

    var canBeNull = true
    var unique = false
    var insertable = true
    var updatable = true
    var fetch = FetchType.EAGER
    var cascade = arrayOfNulls<CascadeType>(0)

    var cascadePersist: Boolean? = null
    var cascadeRefresh: Boolean? = null
    var cascadeMerge: Boolean? = null
    var cascadeDetach: Boolean? = null
    var cascadeRemove: Boolean? = null


    override fun toString(): String {
        return columnName + " on " + entityConfig
    }
}
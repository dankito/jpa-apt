package net.dankito.jpa.apt.config

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import javax.persistence.CascadeType
import javax.persistence.FetchType
import javax.persistence.GenerationType


@JsonIdentityInfo(
        generator = ObjectIdGenerators.UUIDGenerator::class)
class ColumnConfig(val entityConfig: EntityConfig, val property: Property) {

    private constructor() : this(EntityConfig(), Property()) // for Jackson


    var type: Class<*> = property.getType()
    var dataType: DataType? = null

    var columnName = property.field.name // default value, may be overwritten by @Column (or other) annotation
    var tableName: String? = null

    // Id configuration
    var isId: Boolean = false
    var isGeneratedId: Boolean = false
    var generatedIdType = GenerationType.AUTO
    var idGenerator: String? = null
    var generatedIdSequence: String? = null

    var isVersion = false

    var isLob = false

    // column configuration
    var columnDefinition: String? = null
    var length = 255
    var scale = 0
    var precision = 0

    var canBeNull = true
    var unique = false
    var insertable = true
    var updatable = true
    var fetch = FetchType.EAGER

    // Relation configuration
    var relationType: RelationType = RelationType.None

    var targetEntity: EntityConfig? = null
    var targetColumn: ColumnConfig? = null

    var orphanRemoval = false
    var referencedColumnName: String? = null

    var isJoinColumn = false

    var cascade = arrayOfNulls<CascadeType>(0)
    var cascadePersist: Boolean? = null
    var cascadeRefresh: Boolean? = null
    var cascadeMerge: Boolean? = null
    var cascadeDetach: Boolean? = null
    var cascadeRemove: Boolean? = null


    fun isRelationshipColumn(): Boolean {
        return relationType != RelationType.None
    }

    fun isToManyColumn(): Boolean {
        return relationType == RelationType.OneToMany || relationType == RelationType.ManyToMany
    }

    fun isManyToManyColumn(): Boolean {
        return relationType == RelationType.ManyToMany
    }


    override fun toString(): String {
        return columnName + " on " + entityConfig
    }
}
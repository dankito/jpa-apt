package net.dankito.jpa.apt.config


data class JpaEntityConfiguration(val entities: List<EntityConfig>) {

    private constructor() : this(listOf()) // for Jackson

}
package net.dankito.jpa.apt.config


data class JPAEntityConfiguration(val entities: List<EntityConfig>) {

    private constructor() : this(listOf()) // for Jackson

}
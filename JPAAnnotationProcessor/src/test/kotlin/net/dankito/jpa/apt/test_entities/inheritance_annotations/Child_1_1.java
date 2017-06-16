package net.dankito.jpa.apt.test_entities.inheritance_annotations;

import javax.persistence.Entity;


@Entity
public class Child_1_1 extends MappedSuperclass {

    private int anInt;

}

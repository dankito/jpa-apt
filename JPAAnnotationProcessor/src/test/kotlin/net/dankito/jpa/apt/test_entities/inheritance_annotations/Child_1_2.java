package net.dankito.jpa.apt.test_entities.inheritance_annotations;

import javax.persistence.Entity;


@Entity
public class Child_1_2 extends MappedSuperclass {

    private long aLong;

}

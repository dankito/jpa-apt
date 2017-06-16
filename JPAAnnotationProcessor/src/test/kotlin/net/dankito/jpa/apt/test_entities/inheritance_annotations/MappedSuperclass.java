package net.dankito.jpa.apt.test_entities.inheritance_annotations;


import javax.persistence.Id;

@javax.persistence.MappedSuperclass
public class MappedSuperclass {

    @Id
    protected String id;

}

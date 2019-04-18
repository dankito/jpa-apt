package net.dankito.jpa.apt.test_entities.property_annotations;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;


@Entity
public class EnumProperty {

    public enum Gender { FEMALE, MALE }


    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

}

package net.dankito.jpa.apt.test_entities.property_annotations;

import javax.persistence.Column;
import javax.persistence.Entity;


@Entity
public class EntityWithAllPrimitiveValues {

    @Column
    private String aString;

    private int anInt;

    @Column
    private short aShort;

    private long aLong;

    private byte aByte;

    @Column
    private float aFloat;

    private double aDouble;


}

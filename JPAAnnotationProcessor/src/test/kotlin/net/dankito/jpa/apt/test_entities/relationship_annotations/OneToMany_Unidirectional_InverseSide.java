package net.dankito.jpa.apt.test_entities.relationship_annotations;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class OneToMany_Unidirectional_InverseSide {

    @Id
    private String id;

}

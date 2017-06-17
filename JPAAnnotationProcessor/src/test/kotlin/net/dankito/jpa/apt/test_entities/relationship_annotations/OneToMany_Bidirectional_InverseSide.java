package net.dankito.jpa.apt.test_entities.relationship_annotations;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


@Entity
public class OneToMany_Bidirectional_InverseSide {

    @Id
    private String id;

    @ManyToOne
    private OneToMany_Bidirectional_OwningSide owningSide;

}

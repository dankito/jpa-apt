package net.dankito.jpa.apt.test_entities.relationship_annotations;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;


@Entity
public class OneToOne_Unidirectional_OwningSide {

    @Id
    private String id;

    @OneToOne
    private OneToOne_Unidirectional_InverseSide inverseSide;

}

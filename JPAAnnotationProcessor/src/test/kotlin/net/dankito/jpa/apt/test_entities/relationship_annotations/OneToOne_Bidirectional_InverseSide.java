package net.dankito.jpa.apt.test_entities.relationship_annotations;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;


@Entity
public class OneToOne_Bidirectional_InverseSide {

    @Id
    private String id;

    @OneToOne(mappedBy = "inverseSide")
    private OneToOne_Bidirectional_OwningSide owningSide;

}

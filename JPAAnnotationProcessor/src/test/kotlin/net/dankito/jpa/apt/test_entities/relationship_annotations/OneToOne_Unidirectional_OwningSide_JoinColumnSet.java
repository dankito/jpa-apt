package net.dankito.jpa.apt.test_entities.relationship_annotations;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;


@Entity
public class OneToOne_Unidirectional_OwningSide_JoinColumnSet {

    @Id
    private String id;

    @OneToOne
    @JoinColumn // TODO
    private OneToOne_Unidirectional_InverseSide inverseSide;

}

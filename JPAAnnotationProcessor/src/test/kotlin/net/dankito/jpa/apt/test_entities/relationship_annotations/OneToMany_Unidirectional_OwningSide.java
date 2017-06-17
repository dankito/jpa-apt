package net.dankito.jpa.apt.test_entities.relationship_annotations;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;


@Entity
public class OneToMany_Unidirectional_OwningSide {

    @Id
    private String id;

    @OneToMany
    private Collection<OneToMany_Unidirectional_InverseSide> inverseSides;

}

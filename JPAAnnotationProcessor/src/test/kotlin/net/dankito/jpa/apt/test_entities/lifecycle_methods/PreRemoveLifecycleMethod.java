package net.dankito.jpa.apt.test_entities.lifecycle_methods;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PreRemove;


@Entity
public class PreRemoveLifecycleMethod {

    @Id
    private String id;


    @PreRemove
    public void preRemove() { }

}

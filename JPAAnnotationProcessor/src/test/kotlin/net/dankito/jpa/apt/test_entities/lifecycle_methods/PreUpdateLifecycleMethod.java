package net.dankito.jpa.apt.test_entities.lifecycle_methods;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PreUpdate;


@Entity
public class PreUpdateLifecycleMethod {

    @Id
    private String id;


    @PreUpdate
    public void preUpdate() { }

}

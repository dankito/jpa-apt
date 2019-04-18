package net.dankito.jpa.apt.test_entities.lifecycle_methods;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;


@Entity
public class PrePersistLifecycleMethodWithParameters {

    @Id
    private String id;


    @PrePersist
    public void prePersist(Object param1, Object param2) { }

}

package net.dankito.jpa.apt.test_entities.lifecycle_methods;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostPersist;


@Entity
public class PostPersistLifecycleMethod {

    @Id
    private String id;


    @PostPersist
    public void postPersist() { }

}

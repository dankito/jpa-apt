package net.dankito.jpa.apt.test_entities.lifecycle_methods;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostLoad;


@Entity
public class PostLoadLifecycleMethod {

    @Id
    private String id;


    @PostLoad
    public void postLoad() { }

}

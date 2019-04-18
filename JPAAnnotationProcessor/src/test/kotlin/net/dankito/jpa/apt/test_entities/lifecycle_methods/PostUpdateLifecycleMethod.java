package net.dankito.jpa.apt.test_entities.lifecycle_methods;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostUpdate;


@Entity
public class PostUpdateLifecycleMethod {

    @Id
    private String id;


    @PostUpdate
    public void postUpdate() { }

}

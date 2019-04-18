package net.dankito.jpa.apt.test_entities.property_annotations;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class BooleanGetterHas {

    @Id
    private String id;

    @Column
    private boolean children;


    public boolean hasChildren() {
        return children;
    }

    public void setChildren(boolean children) {
        this.children = children;
    }

}

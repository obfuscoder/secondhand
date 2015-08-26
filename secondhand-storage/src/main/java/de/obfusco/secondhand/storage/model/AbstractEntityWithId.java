package de.obfusco.secondhand.storage.model;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
abstract class AbstractEntityWithId extends AbstractEntity {
    @Id
    public int id;

    public void setId(int id) {
        this.id = id;
    }
}

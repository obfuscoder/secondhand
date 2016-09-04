package de.obfusco.secondhand.storage.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
abstract class AbstractEntityWithId extends AbstractEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Integer id;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}

package de.obfusco.secondhand.storage.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import java.util.UUID;

@MappedSuperclass
abstract class AbstractEntityWithUuid extends AbstractEntity {

    @Id
    @Column(unique = true)
    public String id;

    @PrePersist
    protected void prePersist() {
        super.prePersist();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

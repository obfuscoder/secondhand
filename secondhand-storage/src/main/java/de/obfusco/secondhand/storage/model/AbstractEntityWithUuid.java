package de.obfusco.secondhand.storage.model;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import java.util.Date;
import java.util.UUID;

@MappedSuperclass
abstract class AbstractEntityWithUuid extends AbstractEntity {

    @Id
    @Column(unique = true)
    private String id;

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    @PrePersist
    protected void prePersist() {
        super.prePersist();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}

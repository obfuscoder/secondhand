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

@MappedSuperclass
abstract class AbstractEntityWithUuid extends AbstractEntity {

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid2")
    @Column(unique = true)
    private String id;

    public String getId() {
        return id;
    }
}

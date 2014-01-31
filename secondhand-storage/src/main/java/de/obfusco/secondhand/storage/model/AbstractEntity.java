package de.obfusco.secondhand.storage.model;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@MappedSuperclass
abstract class AbstractEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @CreatedDate
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date created;

    @LastModifiedDate
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date modified;

    public Integer getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public Date getModified() {
        return modified;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @PrePersist
    private void prePersist() {
        created = modified = new Date();
    }

    @PreUpdate
    private void preUpdate() {
        modified = new Date();
    }
}

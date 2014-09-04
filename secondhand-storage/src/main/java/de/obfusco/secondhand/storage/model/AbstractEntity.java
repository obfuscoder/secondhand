package de.obfusco.secondhand.storage.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
public class AbstractEntity implements Serializable {
    @CreatedDate
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date created;
    @LastModifiedDate
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date modified;

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
    protected void prePersist() {
        Date now = new Date();
        if (created == null) {
            created = now;
        }
        modified = now;
    }

    @PreUpdate
    protected void preUpdate() {
        modified = new Date();
    }

    protected void setCreated(Date created) {
        this.created = created;
    }
}

package libs.ddd;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
@MappedSuperclass
public abstract class BaseEntity implements Comparable<BaseEntity> {
    @Id
    @Column(name = "id")
    protected UUID id;

    protected BaseEntity() {
    }

    protected BaseEntity(UUID id) {
        this.id = id;
    }

    protected boolean isTransient() {
        return id == null || id.equals(defaultValue());
    }

    protected UUID defaultValue() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (this == obj)
            return true;

        if (!(obj instanceof BaseEntity other))
            return false;

        if (!this.getClass().equals(other.getClass()))
            return false;

        if (this.isTransient() || other.isTransient())
            return false;

        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return (getClass() + (id != null ? id.toString() : "")).hashCode();
    }

    @Override
    public int compareTo(BaseEntity other) {
        if (other == null)
            return 1;

        if (this == other)
            return 0;

        return this.id.compareTo(other.id);
    }
}
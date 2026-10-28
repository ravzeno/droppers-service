package ravzeno.droppers_service.domain.storage.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_settings")
public class AppSettingEntity {
    @Id
    @Column(name = "setting_key")
    private String key;

    @Column(name = "setting_value")
    private String value;

    protected AppSettingEntity() {}

    public AppSettingEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String value() {
        return value;
    }
}

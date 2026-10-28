package org.example.model.accommodation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "amenity_types")
public class AmenityType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,
            unique = true,
            columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private AmenityTypeName name;

    public enum AmenityTypeName {
        SWIMMING_POOL,
        GYM,
        PET_FRIENDLY,
        FREE_WIFI,
        HIGH_SPEED_INTERNET,
        PARKING,
        AIR_CONDITIONING,
        CABLE_TV,
        ELEVATOR,
        SPA,
        HOT_TUB,
        SAUNA,
        FIREPLACE,
        DISHWASHER,
        WASHER,
        DRYER,
        OUTDOOR_GRILL,
        TERRACE,
        SMART_HOME,
        WHEELCHAIR_ACCESSIBLE,
        SECURITY,
        CCTV,
        SAFE,
        GARDEN,
        SOUNDPROOFING,
        SHARED_WORKSPACE,
        ROOM_SERVICE,
        SMOKING_AREA,
        FAMILY_FRIENDLY,
        NON_SMOKING,
        PRIVATE_ENTRANCE,
        CAR_RENTAL,
        BIKE_RENTAL
    }
}

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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "accommodation_types")
public class AccommodationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,
            unique = true,
            columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private AccommodationTypeName name;

    public enum AccommodationTypeName {
        HOUSE,
        APARTMENT,
        VACATION_HOME,
        BUNGALOW,
        VILLA,
        COTTAGE,
        LOFT,
        STUDIO,
        TOWNHOUSE,
        DORM,
        GUEST_HOUSE,
        TREE_HOUSE,
        YURT,
        HOSTEL,
        HOTEL
    }
}

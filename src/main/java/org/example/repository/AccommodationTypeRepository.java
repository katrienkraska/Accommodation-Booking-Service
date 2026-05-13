package org.example.repository;

import org.example.model.accommodation.AccommodationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationTypeRepository extends JpaRepository<AccommodationType, Long> {
}

package com.andesstay.mscatalog.repository;

import com.andesstay.mscatalog.model.Unit;
import com.andesstay.mscatalog.model.UnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogRepository extends JpaRepository<Unit, Long> {

    List<Unit> findByType(UnitType type);

    List<Unit> findByAvailability(Boolean availability);

    List<Unit> findByTypeAndAvailability(UnitType type, Boolean availability);
}
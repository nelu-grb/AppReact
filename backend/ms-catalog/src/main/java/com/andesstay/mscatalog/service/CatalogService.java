package com.andesstay.mscatalog.service;

import com.andesstay.mscatalog.dto.UnitRequest;
import com.andesstay.mscatalog.dto.UnitResponse;
import com.andesstay.mscatalog.messaging.KafkaPublisher;
import com.andesstay.mscatalog.model.Unit;
import com.andesstay.mscatalog.model.UnitType;
import com.andesstay.mscatalog.repository.CatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {
    private final CatalogRepository catalogRepository;
    private final KafkaPublisher kafkaPublisher;

    public CatalogService(CatalogRepository catalogRepository, KafkaPublisher kafkaPublisher) {
        this.catalogRepository = catalogRepository;
        this.kafkaPublisher = kafkaPublisher;
    }

    @Transactional
    public UnitResponse create(UnitRequest request, String actor) {
        Unit unit = catalogRepository.save(toEntity(request, new Unit()));
        kafkaPublisher.publishUnitEvent("UNIT_CREATED", unit, actor);
        return toResponse(unit);
    }

    @Transactional(readOnly = true)
    public UnitResponse getById(Long unitId) {
        return toResponse(findUnit(unitId));
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> find(UnitType type, Boolean availability) {
        List<Unit> units;
        if (type != null && availability != null) {
            units = catalogRepository.findByTypeAndAvailability(type, availability);
        } else if (type != null) {
            units = catalogRepository.findByType(type);
        } else if (availability != null) {
            units = catalogRepository.findByAvailability(availability);
        } else {
            units = catalogRepository.findAll();
        }
        return units.stream().map(this::toResponse).toList();
    }

    @Transactional
    public UnitResponse update(Long unitId, UnitRequest request, String actor) {
        Unit unit = toEntity(request, findUnit(unitId));
        Unit updated = catalogRepository.save(unit);
        kafkaPublisher.publishUnitEvent("UNIT_UPDATED", updated, actor);
        return toResponse(updated);
    }

    @Transactional
    public void delete(Long unitId, String actor) {
        Unit unit = findUnit(unitId);
        catalogRepository.delete(unit);
        kafkaPublisher.publishUnitEvent("UNIT_DELETED", unit, actor);
    }

    private Unit findUnit(Long unitId) {
        return catalogRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada con ID: " + unitId));
    }

    private Unit toEntity(UnitRequest request, Unit unit) {
        unit.setName(request.getName());
        unit.setDescription(request.getDescription());
        unit.setAddress(request.getAddress());
        unit.setType(request.getType());
        unit.setCity(request.getCity());
        unit.setAvailability(request.getAvailability());
        unit.setRooms(request.getRooms());
        unit.setBathrooms(request.getBathrooms());
        unit.setPricePerNight(request.getPricePerNight());
        unit.setMaxOccupancy(request.getMaxOccupancy());
        return unit;
    }

    private UnitResponse toResponse(Unit unit) {
        return new UnitResponse(unit.getUnitId(), unit.getName(), unit.getDescription(), unit.getAddress(),
            unit.getType(), unit.getCity(), unit.getAvailability(), unit.getRooms(), unit.getBathrooms(),
            unit.getPricePerNight(), unit.getMaxOccupancy());
    }
}

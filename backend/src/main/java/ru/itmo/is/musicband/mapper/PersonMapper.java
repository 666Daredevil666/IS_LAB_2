package ru.itmo.is.musicband.mapper;

import org.mapstruct.*;
import ru.itmo.is.musicband.domain.*;
import ru.itmo.is.musicband.dto.*;

@Mapper(config = CentralConfig.class, componentModel = "spring")
public interface PersonMapper {
    @Mapping(target = "location", ignore = true)
    Person toEntity(PersonDto dto);

    @Mapping(target = "locationId", source = "location.id")
    PersonDto toDto(Person entity);
}

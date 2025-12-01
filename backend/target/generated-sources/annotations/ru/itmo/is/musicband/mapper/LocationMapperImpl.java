package ru.itmo.is.musicband.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.itmo.is.musicband.domain.Location;
import ru.itmo.is.musicband.dto.LocationDto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-01T00:53:52+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class LocationMapperImpl implements LocationMapper {

    @Override
    public Location toEntity(LocationDto dto) {
        if ( dto == null ) {
            return null;
        }

        Location location = new Location();

        location.setId( dto.getId() );
        location.setName( dto.getName() );
        if ( dto.getX() != null ) {
            location.setX( dto.getX() );
        }
        location.setY( dto.getY() );

        return location;
    }

    @Override
    public LocationDto toDto(Location entity) {
        if ( entity == null ) {
            return null;
        }

        LocationDto locationDto = new LocationDto();

        locationDto.setId( entity.getId() );
        locationDto.setName( entity.getName() );
        locationDto.setX( entity.getX() );
        locationDto.setY( entity.getY() );

        return locationDto;
    }
}

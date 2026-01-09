package ru.itmo.is.musicband.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.itmo.is.musicband.domain.Location;
import ru.itmo.is.musicband.dto.LocationDto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-09T11:19:14+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
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
        if ( dto.getX() != null ) {
            location.setX( dto.getX() );
        }
        location.setY( dto.getY() );
        location.setName( dto.getName() );

        return location;
    }

    @Override
    public LocationDto toDto(Location entity) {
        if ( entity == null ) {
            return null;
        }

        LocationDto locationDto = new LocationDto();

        locationDto.setId( entity.getId() );
        locationDto.setX( entity.getX() );
        locationDto.setY( entity.getY() );
        locationDto.setName( entity.getName() );

        return locationDto;
    }
}

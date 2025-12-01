package ru.itmo.is.musicband.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.itmo.is.musicband.domain.Album;
import ru.itmo.is.musicband.domain.Coordinates;
import ru.itmo.is.musicband.domain.MusicBand;
import ru.itmo.is.musicband.domain.Person;
import ru.itmo.is.musicband.dto.CoordinatesDto;
import ru.itmo.is.musicband.dto.CreateBandDto;
import ru.itmo.is.musicband.dto.MusicBandDto;
import ru.itmo.is.musicband.dto.UpdateBandDto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-01T00:53:52+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class BandMapperImpl implements BandMapper {

    @Override
    public MusicBand toEntity(CreateBandDto dto) {
        if ( dto == null ) {
            return null;
        }

        MusicBand musicBand = new MusicBand();

        if ( dto.getAlbumsCount() != null ) {
            musicBand.setAlbumsCount( dto.getAlbumsCount() );
        }
        musicBand.setCoordinates( coordinatesDtoToCoordinates( dto.getCoordinates() ) );
        musicBand.setDescription( dto.getDescription() );
        musicBand.setGenre( dto.getGenre() );
        musicBand.setName( dto.getName() );
        musicBand.setNumberOfParticipants( dto.getNumberOfParticipants() );
        if ( dto.getSinglesCount() != null ) {
            musicBand.setSinglesCount( dto.getSinglesCount() );
        }

        return musicBand;
    }

    @Override
    public void updateEntity(UpdateBandDto dto, MusicBand entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getAlbumsCount() != null ) {
            entity.setAlbumsCount( dto.getAlbumsCount() );
        }
        if ( dto.getCoordinates() != null ) {
            if ( entity.getCoordinates() == null ) {
                entity.setCoordinates( new Coordinates() );
            }
            coordinatesDtoToCoordinates1( dto.getCoordinates(), entity.getCoordinates() );
        }
        else {
            entity.setCoordinates( null );
        }
        entity.setDescription( dto.getDescription() );
        entity.setGenre( dto.getGenre() );
        entity.setName( dto.getName() );
        entity.setNumberOfParticipants( dto.getNumberOfParticipants() );
        if ( dto.getSinglesCount() != null ) {
            entity.setSinglesCount( dto.getSinglesCount() );
        }
    }

    @Override
    public MusicBandDto toDto(MusicBand entity) {
        if ( entity == null ) {
            return null;
        }

        MusicBandDto musicBandDto = new MusicBandDto();

        musicBandDto.setBestAlbumId( entityBestAlbumId( entity ) );
        musicBandDto.setBestAlbumName( entityBestAlbumName( entity ) );
        musicBandDto.setFrontManId( entityFrontManId( entity ) );
        musicBandDto.setFrontManName( entityFrontManName( entity ) );
        musicBandDto.setAlbumsCount( entity.getAlbumsCount() );
        musicBandDto.setCoordinates( coordinatesToCoordinatesDto( entity.getCoordinates() ) );
        musicBandDto.setDescription( entity.getDescription() );
        musicBandDto.setGenre( entity.getGenre() );
        musicBandDto.setId( entity.getId() );
        musicBandDto.setName( entity.getName() );
        musicBandDto.setNumberOfParticipants( entity.getNumberOfParticipants() );
        musicBandDto.setSinglesCount( entity.getSinglesCount() );

        musicBandDto.setCreationDate( entity.getCreationDate().toString() );

        return musicBandDto;
    }

    protected Coordinates coordinatesDtoToCoordinates(CoordinatesDto coordinatesDto) {
        if ( coordinatesDto == null ) {
            return null;
        }

        Coordinates coordinates = new Coordinates();

        if ( coordinatesDto.getX() != null ) {
            coordinates.setX( coordinatesDto.getX() );
        }
        if ( coordinatesDto.getY() != null ) {
            coordinates.setY( coordinatesDto.getY() );
        }

        return coordinates;
    }

    protected void coordinatesDtoToCoordinates1(CoordinatesDto coordinatesDto, Coordinates mappingTarget) {
        if ( coordinatesDto == null ) {
            return;
        }

        if ( coordinatesDto.getX() != null ) {
            mappingTarget.setX( coordinatesDto.getX() );
        }
        if ( coordinatesDto.getY() != null ) {
            mappingTarget.setY( coordinatesDto.getY() );
        }
    }

    private Long entityBestAlbumId(MusicBand musicBand) {
        if ( musicBand == null ) {
            return null;
        }
        Album bestAlbum = musicBand.getBestAlbum();
        if ( bestAlbum == null ) {
            return null;
        }
        Long id = bestAlbum.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityBestAlbumName(MusicBand musicBand) {
        if ( musicBand == null ) {
            return null;
        }
        Album bestAlbum = musicBand.getBestAlbum();
        if ( bestAlbum == null ) {
            return null;
        }
        String name = bestAlbum.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long entityFrontManId(MusicBand musicBand) {
        if ( musicBand == null ) {
            return null;
        }
        Person frontMan = musicBand.getFrontMan();
        if ( frontMan == null ) {
            return null;
        }
        Long id = frontMan.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityFrontManName(MusicBand musicBand) {
        if ( musicBand == null ) {
            return null;
        }
        Person frontMan = musicBand.getFrontMan();
        if ( frontMan == null ) {
            return null;
        }
        String name = frontMan.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    protected CoordinatesDto coordinatesToCoordinatesDto(Coordinates coordinates) {
        if ( coordinates == null ) {
            return null;
        }

        CoordinatesDto coordinatesDto = new CoordinatesDto();

        coordinatesDto.setX( coordinates.getX() );
        coordinatesDto.setY( coordinates.getY() );

        return coordinatesDto;
    }
}

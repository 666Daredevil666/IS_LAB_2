package ru.itmo.is.musicband.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.itmo.is.musicband.domain.Album;
import ru.itmo.is.musicband.dto.AlbumDto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-09T11:19:14+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
)
@Component
public class AlbumMapperImpl implements AlbumMapper {

    @Override
    public Album toEntity(AlbumDto dto) {
        if ( dto == null ) {
            return null;
        }

        Album album = new Album();

        album.setId( dto.getId() );
        album.setName( dto.getName() );
        if ( dto.getSales() != null ) {
            album.setSales( dto.getSales() );
        }

        return album;
    }

    @Override
    public AlbumDto toDto(Album entity) {
        if ( entity == null ) {
            return null;
        }

        AlbumDto albumDto = new AlbumDto();

        albumDto.setId( entity.getId() );
        albumDto.setName( entity.getName() );
        albumDto.setSales( entity.getSales() );

        return albumDto;
    }
}

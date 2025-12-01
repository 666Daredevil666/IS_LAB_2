package ru.itmo.is.musicband.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.itmo.is.musicband.domain.Album;
import ru.itmo.is.musicband.dto.AlbumDto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-01T00:53:52+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
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

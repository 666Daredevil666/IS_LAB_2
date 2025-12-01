package ru.itmo.is.musicband.mapper;

import org.mapstruct.*;
import ru.itmo.is.musicband.domain.*;
import ru.itmo.is.musicband.dto.*;

@Mapper(config = CentralConfig.class, componentModel = "spring")
public interface AlbumMapper {
    Album toEntity(AlbumDto dto);

    AlbumDto toDto(Album entity);
}

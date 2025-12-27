package ru.itmo.is.musicband.mapper;

import org.mapstruct.*;
import ru.itmo.is.musicband.domain.*;
import ru.itmo.is.musicband.dto.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlbumMapper {
    Album toEntity(AlbumDto dto);

    AlbumDto toDto(Album entity);
}

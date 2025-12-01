package ru.itmo.is.musicband.mapper;

import org.mapstruct.*;
import ru.itmo.is.musicband.domain.*;
import ru.itmo.is.musicband.dto.*;

@Mapper(config = CentralConfig.class, componentModel = "spring")
public interface BandMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "bestAlbum", ignore = true)
    @Mapping(target = "frontMan", ignore = true)
    MusicBand toEntity(CreateBandDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "bestAlbum", ignore = true)
    @Mapping(target = "frontMan", ignore = true)
    void updateEntity(UpdateBandDto dto, @MappingTarget MusicBand entity);

    @Mapping(target = "bestAlbumId", source = "bestAlbum.id")
    @Mapping(target = "bestAlbumName", source = "bestAlbum.name")
    @Mapping(target = "frontManId", source = "frontMan.id")
    @Mapping(target = "frontManName", source = "frontMan.name")
    @Mapping(target = "creationDate", expression = "java(entity.getCreationDate().toString())")
    MusicBandDto toDto(MusicBand entity);
}

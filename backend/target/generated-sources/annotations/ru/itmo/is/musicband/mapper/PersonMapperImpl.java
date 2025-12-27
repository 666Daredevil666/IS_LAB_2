package ru.itmo.is.musicband.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.itmo.is.musicband.domain.Location;
import ru.itmo.is.musicband.domain.Person;
import ru.itmo.is.musicband.dto.PersonDto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-27T15:51:27+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class PersonMapperImpl implements PersonMapper {

    @Override
    public Person toEntity(PersonDto dto) {
        if ( dto == null ) {
            return null;
        }

        Person person = new Person();

        person.setBirthday( dto.getBirthday() );
        person.setEyeColor( dto.getEyeColor() );
        person.setHairColor( dto.getHairColor() );
        person.setId( dto.getId() );
        person.setName( dto.getName() );
        person.setNationality( dto.getNationality() );
        person.setWeight( dto.getWeight() );

        return person;
    }

    @Override
    public PersonDto toDto(Person entity) {
        if ( entity == null ) {
            return null;
        }

        PersonDto personDto = new PersonDto();

        personDto.setLocationId( entityLocationId( entity ) );
        personDto.setBirthday( entity.getBirthday() );
        personDto.setEyeColor( entity.getEyeColor() );
        personDto.setHairColor( entity.getHairColor() );
        personDto.setId( entity.getId() );
        personDto.setName( entity.getName() );
        personDto.setNationality( entity.getNationality() );
        personDto.setWeight( entity.getWeight() );

        return personDto;
    }

    private Long entityLocationId(Person person) {
        if ( person == null ) {
            return null;
        }
        Location location = person.getLocation();
        if ( location == null ) {
            return null;
        }
        Long id = location.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

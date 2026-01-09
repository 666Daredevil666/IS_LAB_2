package ru.itmo.is.musicband.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.itmo.is.musicband.domain.Location;
import ru.itmo.is.musicband.domain.Person;
import ru.itmo.is.musicband.dto.PersonDto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-09T11:19:14+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
)
@Component
public class PersonMapperImpl implements PersonMapper {

    @Override
    public Person toEntity(PersonDto dto) {
        if ( dto == null ) {
            return null;
        }

        Person person = new Person();

        person.setId( dto.getId() );
        person.setName( dto.getName() );
        person.setEyeColor( dto.getEyeColor() );
        person.setHairColor( dto.getHairColor() );
        person.setBirthday( dto.getBirthday() );
        person.setWeight( dto.getWeight() );
        person.setNationality( dto.getNationality() );

        return person;
    }

    @Override
    public PersonDto toDto(Person entity) {
        if ( entity == null ) {
            return null;
        }

        PersonDto personDto = new PersonDto();

        personDto.setLocationId( entityLocationId( entity ) );
        personDto.setId( entity.getId() );
        personDto.setName( entity.getName() );
        personDto.setEyeColor( entity.getEyeColor() );
        personDto.setHairColor( entity.getHairColor() );
        personDto.setBirthday( entity.getBirthday() );
        personDto.setWeight( entity.getWeight() );
        personDto.setNationality( entity.getNationality() );

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

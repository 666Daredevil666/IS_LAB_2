package ru.itmo.is.musicband.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.is.musicband.domain.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}

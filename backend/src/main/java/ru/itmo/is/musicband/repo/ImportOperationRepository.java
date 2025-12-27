package ru.itmo.is.musicband.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.is.musicband.domain.ImportOperation;

import java.util.List;

@Repository
public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long> {
    @Query(value = "SELECT * FROM import_operation WHERE user_name = :userName ORDER BY id DESC", nativeQuery = true)
    List<ImportOperation> findByUserName(@Param("userName") String userName);
}


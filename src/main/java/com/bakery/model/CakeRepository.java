package com.bakery.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CakeRepository extends JpaRepository<Cake, Long> {

    List<Cake> findAllByOrderByOrdenAsc();

    Optional<Cake> findBySlug(String slug);

    boolean existsBySlug(String slug);
}

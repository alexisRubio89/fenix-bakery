package com.bakery.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findAllByOrderByOrdenAsc();

    Optional<MenuItem> findBySlug(String slug);

    boolean existsBySlug(String slug);
}

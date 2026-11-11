package cz.uhk.fim.ppro.warehouse.repository;

import cz.uhk.fim.ppro.warehouse.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repozitář pro entitu kategorie {@link Category}.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Vyhledá kategorii podle jejího unikátního slugu (např. "pro-batolata").
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Ověří existenci kategorie podle slugu.
     */
    boolean existsBySlug(String slug);
}


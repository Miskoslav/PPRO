package cz.uhk.fim.ppro.warehouse.api;

import cz.uhk.fim.ppro.warehouse.model.Category;
import cz.uhk.fim.ppro.warehouse.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller pro kategorie hraček e-shopu Dřevěnka s.r.o.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Kategorie", description = "Přehled kategorií sortimentu (vazba M:N)")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    @Operation(summary = "Získat seznam kategorií", description = "Vrátí všechny kategorie pro zařazení produktů.")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }
}


package com.plate.silverplate.nutritionFact.domain.repo;

import com.plate.silverplate.nutritionFact.domain.entity.NutritionFact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionFactRepository extends JpaRepository<NutritionFact,Long> {
    Long countBy();

}

package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Integer>, JpaSpecificationExecutor<Car> {
    List<Car> findAllBySourceIgnoreCase(String source);
    Car findByEnglishNameIgnoreCase(String englishName);
}

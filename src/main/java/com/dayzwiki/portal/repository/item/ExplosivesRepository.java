package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Explosives;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExplosivesRepository extends JpaRepository<Explosives, Integer>, JpaSpecificationExecutor<Explosives> {
}

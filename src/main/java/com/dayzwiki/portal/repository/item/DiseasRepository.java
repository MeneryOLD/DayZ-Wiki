package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Diseas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DiseasRepository extends JpaRepository<Diseas, Integer>, JpaSpecificationExecutor<Diseas> {

}

package com.daurenassanbaev.ideaservice.database.repository;

import com.daurenassanbaev.ideaservice.database.entity.Idea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IdeaRepository extends JpaRepository<Idea, Integer> {
    @Modifying
    @Transactional
    @Query("update Idea i SET i.rating=:rating where i.id=:id")
    void updateById(Integer id, Double rating);
}

package com.daurenassanbaev.ratingsservice.database.repository;

import com.daurenassanbaev.ratingsservice.database.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingsRepository extends JpaRepository<Rating, Integer> {
    Optional<Rating> findByIdeaIdAndUserId(Integer ideaId, String userId);
    List<Rating> findAllByIdeaId(Integer ideaId);
    @Query("SELECT avg(r.score) FROM Rating r WHERE r.ideaId=:ideaId")
    Double calculateAverageScore(Integer ideaId);

}

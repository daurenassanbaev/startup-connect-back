package com.daurenassanbaev.userservice.db.repository;

import com.daurenassanbaev.userservice.db.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Integer> {
    Optional<Photo> findByUserId(String userId);

    @Query("DELETE FROM Photo p WHERE p.userId = :userId AND p.url LIKE %:filename%")
    void deleteByUserIdAndUrl(String userId, String filename);
}

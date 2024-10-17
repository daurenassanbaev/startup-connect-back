package com.daurenassanbaev.ideaservice.database.repository;

import com.daurenassanbaev.ideaservice.database.entity.IdeaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface IdeaFileRepository extends JpaRepository<IdeaFile, Integer> {
    Optional<IdeaFile> findByUserId(String userId);
    @Modifying
    @Transactional
    @Query("delete from IdeaFile i where i.userId = :userId and i.url like concat('%', :url, '%')")
    void deleteByUserIdAndUrl(String userId, String url);
}

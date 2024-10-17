package com.daurenassanbaev.ideaservice.mapper;

import com.daurenassanbaev.ideaservice.database.dto.IdeaDto;
import com.daurenassanbaev.ideaservice.database.entity.Idea;
import org.springframework.stereotype.Component;

@Component
public class IdeaMapper implements Mapper<IdeaDto, Idea> {
    public Idea map(IdeaDto object, String userId) {
        Idea idea = new Idea();
        idea.setUserId(userId);
        copy(object, idea);
        return idea;
    }
    public void copy(IdeaDto ideaDto, Idea idea) {
        idea.setDescription(ideaDto.getDescription());
        idea.setTitle(ideaDto.getTitle());
    }

    @Override
    public Idea map(IdeaDto object) {
        return null;
    }
}

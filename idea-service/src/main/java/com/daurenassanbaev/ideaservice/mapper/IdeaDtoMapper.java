package com.daurenassanbaev.ideaservice.mapper;

import com.daurenassanbaev.ideaservice.database.dto.IdeaDto;
import com.daurenassanbaev.ideaservice.database.entity.Idea;
import org.springframework.stereotype.Component;

@Component
public class IdeaDtoMapper implements Mapper<Idea, IdeaDto> {
    @Override
    public IdeaDto map(Idea object) {
        IdeaDto dto = new IdeaDto();
        copy(object, dto);
        return dto;
    }
    public void copy(Idea idea, IdeaDto ideaDto) {
        ideaDto.setDescription(idea.getDescription());
        ideaDto.setTitle(idea.getTitle());
    }
}

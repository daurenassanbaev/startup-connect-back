package com.daurenassanbaev.commentservice.mapper;

import com.daurenassanbaev.commentservice.db.dto.CommentDto;
import com.daurenassanbaev.commentservice.db.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentDtoMapper implements Mapper<Comment, CommentDto> {

    @Override
    public CommentDto map(Comment object) {
        CommentDto comment = new CommentDto();
        copy(object, comment);
        return comment;
    }

    public void copy(Comment dto, CommentDto comment) {
        comment.setIdeaId(dto.getIdeaId());
        comment.setUserId(dto.getUserId());
        comment.setContent(dto.getContent());
    }
}

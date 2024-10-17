package com.daurenassanbaev.commentservice.mapper;

import com.daurenassanbaev.commentservice.db.dto.CommentDto;
import com.daurenassanbaev.commentservice.db.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper implements Mapper<CommentDto, Comment> {

    @Override
    public Comment map(CommentDto object) {
        Comment comment = new Comment();
        copy(object, comment);
        return comment;
    }

    public void copy(CommentDto dto, Comment comment) {
        comment.setIdeaId(dto.getIdeaId());
        comment.setUserId(dto.getUserId());
        comment.setContent(dto.getContent());
    }
}

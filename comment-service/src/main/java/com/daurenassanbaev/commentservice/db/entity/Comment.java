package com.daurenassanbaev.commentservice.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.RowId;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
@Getter
@Setter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "idea_id")
    private Integer ideaId;

    @Column(name = "content")
    private String content;
}

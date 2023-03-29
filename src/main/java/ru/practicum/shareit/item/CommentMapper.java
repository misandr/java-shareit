package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {

        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setItem(comment.getItem().getId());
        commentDto.setCreated(comment.getCreated());
        commentDto.setAuthorName(comment.getAuthor().getName());

        return commentDto;
    }

    public static List<CommentDto> toCommentsDto(List<Comment> comments) {
        List<CommentDto> commentsDto = new ArrayList<>();

        for (Comment comment : comments) {
            commentsDto.add(CommentMapper.toCommentDto(comment));
        }

        return commentsDto;
    }
}

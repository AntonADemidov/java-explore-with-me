package ru.practicum.ewm.comment.mapper;

import ru.practicum.ewm.comment.model.Message;
import ru.practicum.ewm.comment.model.MessageDto;

import java.time.format.DateTimeFormatter;

public class MessageMapper {
    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static MessageDto toMessageDto(Message message) {
        MessageDto messageDto = new MessageDto();
        messageDto.setId(message.getId());
        messageDto.setCommentId(message.getComment().getId());
        messageDto.setSenderId(message.getSenderId());
        messageDto.setText(message.getText());
        messageDto.setCreatedOn(message.getCreatedOn().format(FORMATTER));
        return messageDto;
    }
}

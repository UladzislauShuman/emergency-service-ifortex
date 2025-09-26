package ifortex.shuman.uladzislau.authservice.paramedic.dto;

public record DocumentDataDto(
    byte[] content,
    String filename,
    String contentType
) {}
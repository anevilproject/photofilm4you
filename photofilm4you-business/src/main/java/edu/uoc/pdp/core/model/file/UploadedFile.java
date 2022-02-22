package edu.uoc.pdp.core.model.file;

public class UploadedFile {

    private final String contentType;
    private final String name;
    private final byte[] content;
    private final long size;

    public UploadedFile(String contentType, String name, byte[] content) {
        this.contentType = contentType;
        this.name = name;
        this.content = content;
        size = content.length;
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }

    public byte[] getContent() {
        return content;
    }

    public long getSize() {
        return size;
    }
}

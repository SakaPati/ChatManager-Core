package ru.fozeton.chatmanager.module.gif;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.google.gson.annotations.SerializedName;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GifResponse {

    private boolean result;
    private ResponseData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseData {
        private List<MediaItem> data;

        @SerializedName("current_page")
        private int currentPage;

        @SerializedName("per_page")
        private int perPage;

        @SerializedName("has_next")
        private boolean hasNext;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaItem {
        private long id;
        private String slug;
        private String title;
        private FileContainer file;
        private List<String> tags;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileContainer {
        private ImageDetails hd;
        private ImageDetails md;
        private ImageDetails sm;
        private ImageDetails xs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDetails {
        private String url;
        private int width;
        private int height;
        private long size;
    }
}
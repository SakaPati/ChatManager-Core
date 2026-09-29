package ru.fozeton.chatmanager.module.gif;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GifsResponse {
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

        @SerializedName("blur_preview")
        private String blurPreview;
    }
}
package ru.fozeton.chatmanager.module.gif;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GifResponse {
    private boolean result;
    private MediaItem data;

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

        @SerializedName("total_shares")
        private long totalShares;
        @SerializedName("blur_preview")
        private String blurPreview;
    }
}
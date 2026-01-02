package com.pet.dto;

import jakarta.validation.constraints.Size;

public record UpdateSitterRecordDto(
        @Size(max = 200, message = "活動描述長度不能超過200個字元") String activity,

        Boolean fed,

        Boolean walked,

        @Size(max = 50, message = "心情狀態長度不能超過50個字元") String moodStatus,

        @Size(max = 1000, message = "備註長度不能超過1000個字元") String notes,

        @Size(max = 500, message = "照片路徑長度不能超過500個字元") String photos) {
}

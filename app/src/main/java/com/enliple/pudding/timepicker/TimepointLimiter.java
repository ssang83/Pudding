package com.enliple.pudding.timepicker;

import android.os.Parcelable;

@SuppressWarnings("WeakerAccess")
public interface TimepointLimiter extends Parcelable {
    boolean isOutOfRange(@androidx.annotation.Nullable Timepoint point, int index, @androidx.annotation.NonNull Timepoint.TYPE resolution);

    boolean isAmDisabled();

    boolean isPmDisabled();

    @androidx.annotation.NonNull
    Timepoint roundToNearest(
            @androidx.annotation.NonNull Timepoint time,
            @androidx.annotation.Nullable Timepoint.TYPE type,
            @androidx.annotation.NonNull Timepoint.TYPE resolution
    );
}
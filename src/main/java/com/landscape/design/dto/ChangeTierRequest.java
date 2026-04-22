package com.landscape.design.dto;

import com.landscape.design.domain.SubscriptionTier;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeTierRequest {

    @NotNull
    private SubscriptionTier tier;

    /** Срок окончания подписки; null — без срока (удобно для демо) */
    private String periodEndIso;
}

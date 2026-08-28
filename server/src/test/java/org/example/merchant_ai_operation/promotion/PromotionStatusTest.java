package org.example.merchant_ai_operation.promotion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromotionStatusTest {

    @Test
    void draftCanBeScheduledOrCancelled() {
        assertTrue(
                PromotionStatus.DRAFT.canTransitionTo(PromotionStatus.SCHEDULED)
        );
        assertTrue(
                PromotionStatus.DRAFT.canTransitionTo(PromotionStatus.CANCELLED)
        );
    }

    @Test
    void endedCannotReturnToActive() {
        assertFalse(
                PromotionStatus.ENDED.canTransitionTo(PromotionStatus.ACTIVE)
        );
    }

    @Test
    void nullTargetIsRejected() {
        assertFalse(
                PromotionStatus.DRAFT.canTransitionTo(null)
        );
    }
}
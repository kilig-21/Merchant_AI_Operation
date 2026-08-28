package org.example.merchant_ai_operation.promotion;

public enum PromotionStatus {
    DRAFT,
    SCHEDULED,
    ACTIVE,
    ENDED,
    CANCELLED;


    public boolean canTransitionTo(PromotionStatus target) {
        if(target == null){
            return false;
        }

        return switch (this) {
            case DRAFT -> target == SCHEDULED || target == CANCELLED;
            case SCHEDULED -> target == ACTIVE || target == CANCELLED;
            case ACTIVE -> target == ENDED;
            case ENDED, CANCELLED -> false;
        };
    }
}

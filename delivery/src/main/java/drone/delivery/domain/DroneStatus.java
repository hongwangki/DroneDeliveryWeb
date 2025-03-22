package drone.delivery.domain;

public enum DroneStatus {
    IDLE,        // 대기 중
    TAKING_OFF,  // 이륙 중
    IN_FLIGHT,   // 비행 중
    LANDING,     // 착륙 중
    CHARGING,    // 충전 중
    ERROR        // 오류 발생
}

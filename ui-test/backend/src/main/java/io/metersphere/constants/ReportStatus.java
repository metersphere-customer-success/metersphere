package io.metersphere.constants;

public enum ReportStatus {
    /**
     * 未执行
     */
    PENDING,
    /**
     * 运行中
     */
    RUNNING,
    /**
     * 重试中
     */
    RERUNNING,
    /**
     * 错误
     */
    ERROR,
    /**
     * 成功
     */
    SUCCESS,
    /**
     * 手动停止或者其他停止
     */
    STOPPED,
    /**
     * 超时
     */
    TIMEOUT
}

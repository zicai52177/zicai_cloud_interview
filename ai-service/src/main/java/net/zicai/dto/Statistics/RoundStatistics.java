package net.zicai.dto.Statistics;

/**
 * @author wangdi
 * @date 2026/5/17 14:59
 * @description
 */

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 统计轮次信息
 */
@Getter
@AllArgsConstructor
public class RoundStatistics {
    /** 总题⽬数 */
    private final int totalQuestions;
    /** 已答题数 */
    private final int answeredQuestions;
    /** 已完成轮次数 */
    private final int completedRounds;
    /** 综合平均分 */
    private final BigDecimal averageScore;
}

package net.zicai.dto.Statistics;

/**
 * @author wangdi
 * @date 2026/5/17 14:59
 * @description
 */

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题⽬统计信息
 */
@Getter
@AllArgsConstructor
public class QuestionStatistics {
    /** 所有题⽬的答题详情字符串（传给AI⽣成轮次评价） */
    private final String questionsInfo;
    /** 平均分数 */
    private final double averageScore;
}
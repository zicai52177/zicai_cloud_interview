package net.zicai.mapper;

import net.zicai.model.InterviewRoundDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 紫菜,
 * @since 2026-04-14
 */
public interface InterviewRoundMapper extends BaseMapper<InterviewRoundDO> {

    void insertBatch(@Param("roundDOList") List<InterviewRoundDO> roundDOList);
}

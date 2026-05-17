package net.zicai.mapper;

import net.zicai.dto.QuestionDTO;
import net.zicai.model.QuestionDO;
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
public interface QuestionMapper extends BaseMapper<QuestionDO> {

    void insertBatch(@Param("questions") List<QuestionDO> questions);
}

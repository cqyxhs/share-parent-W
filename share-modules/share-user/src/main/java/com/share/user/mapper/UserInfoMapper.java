package com.share.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.share.user.domain.UserCountVo;
import com.share.user.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo>
{

    /**
     * 查询用户列表
     *
     * @param userInfo 用户
     * @return 用户集合
     */
    List<UserInfo> selectUserInfoList(UserInfo userInfo);


    /**
     * 查询指定年份区间的用户注册数量
     * @param startYear 开始年份
     * @param endYear 结束年份
     * @return 用户注册统计数据
     */
    List<UserCountVo> selectUserCountBetween(@Param("startYear") Integer startYear,@Param("endYear") Integer endYear);
}

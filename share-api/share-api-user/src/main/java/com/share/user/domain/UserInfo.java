package com.share.user.domain;

import com.share.common.core.annotation.Excel;
import com.share.common.core.web.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.Date;

/**
 * 用户对象 user_info
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户")
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo extends BaseEntity
{
    @Serial
    private static final long serialVersionUID = 1L;

    /** 微信openId */
    @Excel(name = "微信openId")
    @Schema(description = "微信openId")
    private String wxOpenId;

    /** 会员昵称 */
    @Excel(name = "会员昵称")
    @Schema(description = "会员昵称")
    private String nickname;

    /** 性别 */
    @Excel(name = "性别")
    @Schema(description = "性别")
    private String gender;

    /** 头像 */
    @Excel(name = "头像")
    @Schema(description = "头像")
    private String avatarUrl;

    /** 电话 */
    @Excel(name = "电话")
    @Schema(description = "电话")
    private String phone;

    /** 1有效，2禁用 */
    @Excel(name = "1有效，2禁用")
    @Schema(description = "1有效，2禁用")
    private String status;

    @Excel(name = "最后登录IP地址")
    @Schema(description = "最后登录ip")
    private String LastLoginIp;

    @Excel(name = "最后登录时间")
    @Schema(description = "最后登录时间")
    private Date LastLoginTime;

    @Excel(name = "押金状态")
    @Schema(description = "押金状态（0：未验证 1：免押金 2：已交押金）")
    private String depositStatus;
}

package com.share.common.log.service;

import com.share.common.core.constant.SecurityConstants;
import com.share.system.api.RemoteLogService;
import com.share.system.api.domain.SysOperLog;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步调用日志服务
 */
@Service
public class AsyncLogService
{
    @Resource
    private RemoteLogService remoteLogService;

    /**
     * 保存系统日志记录
     */
    @Async
    public void saveSysLog(SysOperLog sysOperLog) throws Exception
    {
        remoteLogService.saveLog(sysOperLog, SecurityConstants.INNER);
    }
}

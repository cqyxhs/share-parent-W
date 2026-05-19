package com.share.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.device.domain.CabinetSlot;

/**
 * 柜机插槽Service接口
 *
 * @author atguigu
 * @date 2024-10-22
 */
public interface ICabinetSlotService extends IService<CabinetSlot>
{

    CabinetSlot getBtSlotNo(Long cabinetId, String slotNo);

}

import request from '@/utils/request'

// 查询全部费用规则列表（无分页）
export function listFeeRuleAll() {
    return request({
        url: '/rule/feeRule/getALLFeeRuleList',
        method: 'get'
    })
}

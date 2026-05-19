import request from '@/utils/request'

// 统计指定年份之间每月用户注册数据
export function getUserCount(startYear, endYear) {
  return request({
    url: `/statics/userCount/${startYear}/${endYear}`,
    method: 'get'
  })
}

// 统计订单数据
export function getOrderCount(message) {
  return request({
    url: '/statics/orderData?message='+message,
    method: 'get'
  })
}
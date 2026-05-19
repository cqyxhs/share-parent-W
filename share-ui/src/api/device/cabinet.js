import request from '@/utils/request'

// 查询柜机列表
export function listCabinet(query) {
    return request({
        url: '/device/cabinet/list',
        method: 'get',
        params: query
    })
}

// 查询柜机详细
export function getCabinet(id) {
    return request({
        url: '/device/cabinet/' + id,
        method: 'get'
    })
}

// 新增柜机
export function addCabinet(data) {
    return request({
        url: '/device/cabinet',
        method: 'post',
        data: data
    })
}

// 修改柜机
export function updateCabinet(data) {
    return request({
        url: '/device/cabinet',
        method: 'put',
        data: data
    })
}

// 删除柜机
export function delCabinet(id) {
    return request({
        url: '/device/cabinet/' + id,
        method: 'delete'
    })
}

// 查询未使用的柜机列表
export function searchNoUseCabinet(keyword) {
    return request({
        url: '/device/cabinet/searchNoUseList/' + keyword,
        method: 'get'
    })
}

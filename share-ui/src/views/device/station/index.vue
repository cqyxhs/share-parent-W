<template>
  <div class="app-container">

    <!-- 搜索表单 -->
    <el-form ref="queryRef" :inline="true" label-width="80px">
      <el-form-item label="站点名称" prop="name">
        <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入站点名称"
        />
      </el-form-item>
      <el-form-item label="负责人" prop="headName">
        <el-input
            v-model="queryParams.headName"
            clearable
            placeholder="请输入负责人"
        />
      </el-form-item>
      <el-form-item label="联系电话" prop="headPhone">
        <el-input
            v-model="queryParams.headPhone"
            clearable
            placeholder="请输入联系电话"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="Search" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" type="danger" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 功能按钮栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            icon="Plus"
            plain
            type="primary"
            @click="handleAdd"
            v-hasPermi="['device:station:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            icon="Delete"
            plain
            type="danger"
            @click="handleDelete"
        >删除
        </el-button>
      </el-col>
    </el-row>

    <!-- 数据展示表格 -->
    <el-table v-loading="loading" :data="stationList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column label="站点名称" prop="name" width="160"/>
      <el-table-column label="地址" prop="fullAddress" min-width="200"/>
      <el-table-column label="负责人" prop="headName" width="100"/>
      <el-table-column label="联系电话" prop="headPhone" width="120"/>
      <el-table-column label="关联柜机ID" prop="cabinetId" width="100" align="center"/>
      <el-table-column label="状态" prop="status" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '1'" type="success">正常</el-tag>
          <el-tag v-else type="info">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="180">
        <template #default="scope">
          <el-button icon="Check" round type="success" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button icon="Delete" round type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页条组件 -->
    <pagination
        v-if="total>0"
        v-show="total>0"
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNum"
        :total="total"
        @pagination="getList"
    />

    <!-- 添加或修改站点对话框 -->
    <el-dialog v-model="open" :title="title" append-to-body width="700px">
      <el-form ref="stationRef" :model="form" :rules="rules" label-width="120px">

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="站点名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入站点名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="营业时间" prop="businessHours">
              <el-input v-model="form.businessHours" placeholder="例如: 08:00-22:00"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="站点图片" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="请输入图片URL地址"/>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="省" prop="provinceCode">
              <el-select
                  v-model="form.provinceCode"
                  placeholder="请选择省"
                  style="width: 100%"
                  @change="onProvinceChange"
              >
                <el-option
                    v-for="item in provinceOptions"
                    :key="item.code"
                    :label="item.name"
                    :value="item.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="市" prop="cityCode">
              <el-select
                  v-model="form.cityCode"
                  placeholder="请选择市"
                  style="width: 100%"
                  @change="onCityChange"
              >
                <el-option
                    v-for="item in cityOptions"
                    :key="item.code"
                    :label="item.name"
                    :value="item.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区" prop="districtCode">
              <el-select
                  v-model="form.districtCode"
                  placeholder="请选择区"
                  style="width: 100%"
              >
                <el-option
                    v-for="item in districtOptions"
                    :key="item.code"
                    :label="item.name"
                    :value="item.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址"/>
        </el-form-item>

        <el-form-item label="完整地址" prop="fullAddress">
          <el-input v-model="form.fullAddress" placeholder="请输入完整地址"/>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number v-model="form.longitude" :precision="6" :step="0.01" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number v-model="form.latitude" :precision="6" :step="0.01" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="headName">
              <el-input v-model="form.headName" placeholder="请输入负责人"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="headPhone">
              <el-input v-model="form.headPhone" placeholder="请输入联系电话"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="关联柜机" prop="cabinetId">
              <el-select
                  v-model="form.cabinetId"
                  clearable
                  filterable
                  placeholder="请选择柜机"
                  style="width: 100%"
              >
                <el-option
                    v-for="item in cabinetOptions"
                    :key="item.id"
                    :label="item.cabinetNo"
                    :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="费用规则" prop="feeRuleId">
              <el-select
                  v-model="form.feeRuleId"
                  clearable
                  placeholder="请选择费用规则"
                  style="width: 100%"
              >
                <el-option
                    v-for="item in feeRuleOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="状态" prop="status">
          <el-select
              v-model="form.status"
              placeholder="请选择状态"
              style="width: 100%"
          >
            <el-option label="正常" value="1"/>
            <el-option label="停用" value="0"/>
          </el-select>
        </el-form-item>

      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script name="Station" setup>
import {
  listStation,
  addStation,
  getStation,
  updateStation,
  delStation
} from "@/api/device/station";
import { searchNoUseCabinet } from "@/api/device/cabinet";
import { listFeeRuleAll } from "@/api/rule/feeRule";
import {ElMessage, ElMessageBox} from "element-plus";
import {Check, Delete} from "@element-plus/icons-vue";
import request from '@/utils/request'

const stationList = ref([]);
const total = ref(0);
const loading = ref(true);

const open = ref(false);
const title = ref("");

const ids = ref([]);
const single = ref(true);
const multiple = ref(true);

const provinceOptions = ref([]);
const cityOptions = ref([]);
const districtOptions = ref([]);
const cabinetOptions = ref([]);
const feeRuleOptions = ref([]);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    headName: null,
    headPhone: null
  },
  form: {},
  rules: {
    name: [
      { required: true, message: "站点名称不能为空", trigger: "blur" }
    ]
  }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询站点列表 */
function getList() {
  loading.value = true;
  listStation(queryParams.value).then(response => {
    stationList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 加载省份列表 */
function getProvinces() {
  request({
    url: '/device/region/treeSelect/0',
    method: 'get'
  }).then(response => {
    provinceOptions.value = response.data || [];
  });
}

/** 加载未关联的柜机列表 */
function getUnusedCabinets() {
  searchNoUseCabinet('').then(response => {
    cabinetOptions.value = response.data || [];
  });
}

/** 加载费用规则列表 */
function getFeeRules() {
  listFeeRuleAll().then(response => {
    feeRuleOptions.value = response.data || [];
  });
}

/** 省份变更 */
function onProvinceChange() {
  form.value.cityCode = null;
  form.value.districtCode = null;
  cityOptions.value = [];
  districtOptions.value = [];
  if (form.value.provinceCode) {
    request({
      url: '/device/region/treeSelect/' + form.value.provinceCode,
      method: 'get'
    }).then(response => {
      cityOptions.value = response.data || [];
    });
  }
}

/** 城市变更 */
function onCityChange() {
  form.value.districtCode = null;
  districtOptions.value = [];
  if (form.value.cityCode) {
    request({
      url: '/device/region/treeSelect/' + form.value.cityCode,
      method: 'get'
    }).then(response => {
      districtOptions.value = response.data || [];
    });
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  queryParams.value.pageNum = 1
  queryParams.value.pageSize = 10
  queryParams.value.name = null
  queryParams.value.headName = null
  queryParams.value.headPhone = null
  handleQuery();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    name: null,
    imageUrl: null,
    businessHours: null,
    provinceCode: null,
    cityCode: null,
    districtCode: null,
    address: null,
    fullAddress: null,
    longitude: null,
    latitude: null,
    headName: null,
    headPhone: null,
    cabinetId: null,
    feeRuleId: null,
    status: '1',
    remark: null
  };
  cityOptions.value = [];
  districtOptions.value = [];
}

// 新增按钮操作
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加站点";
  getProvinces();
  getUnusedCabinets();
  getFeeRules();
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 修改按钮操作
function handleUpdate(row) {
  reset();
  const _id = row.id
  getStation(_id).then(response => {
    const data = response.data;
    form.value = data;
    open.value = true;
    title.value = "修改站点";

    // 加载地区级联数据
    getProvinces();
    if (data.provinceCode) {
      request({
        url: '/device/region/treeSelect/' + data.provinceCode,
        method: 'get'
      }).then(response => {
        cityOptions.value = response.data || [];
      });
    }
    if (data.cityCode) {
      request({
        url: '/device/region/treeSelect/' + data.cityCode,
        method: 'get'
      }).then(response => {
        districtOptions.value = response.data || [];
      });
    }
    getUnusedCabinets();
    getFeeRules();
  });
}

// 提交按钮
function submitForm() {
  if (form.value.id != null) {
    updateStation(form.value).then(response => {
      ElMessage.success("修改成功");
      open.value = false;
      getList();
    });
  } else {
    addStation(form.value).then(response => {
      ElMessage.success("新增成功")
      open.value = false;
      getList();
    });
  }
}

// 删除按钮操作
function handleDelete(row) {
  const _ids = row.id || ids.value;
  ElMessageBox.confirm('是否确认删除站点编号为"' + _ids + '"的数据项？', "系统提示", {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: "warning",
  }).then(function () {
    return delStation(_ids);
  }).then(() => {
    getList();
    ElMessage.success("删除成功");
  }).catch(() => {
  });
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

getList()
</script>

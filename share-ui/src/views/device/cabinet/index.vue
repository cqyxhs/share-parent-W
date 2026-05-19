<template>
  <div class="app-container">

    <!-- 搜索表单 -->
    <el-form ref="queryRef" :inline="true" label-width="68px">
      <el-form-item label="编号" prop="cabinetNo">
        <el-input
            v-model="queryParams.cabinetNo"
            clearable
            placeholder="请输入柜机编号"
        />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入名称"
        />
      </el-form-item>
      <el-form-item label="类型" prop="cabinetTypeId">
        <el-select
            v-model="queryParams.cabinetTypeId"
            clearable
            placeholder="请选择类型"
            style="width: 150px"
        >
          <el-option
              v-for="item in cabinetTypeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
            v-model="queryParams.status"
            clearable
            placeholder="请选择状态"
            style="width: 120px"
        >
          <el-option label="未投放" value="0"/>
          <el-option label="使用中" value="1"/>
          <el-option label="故障" value="-1"/>
        </el-select>
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
            v-hasPermi="['device:cabinet:add']"
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
    <el-table v-loading="loading" :data="cabinetList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column label="柜机编号" prop="cabinetNo" width="120"/>
      <el-table-column label="名称" prop="name" width="150"/>
      <el-table-column label="柜机类型" prop="cabinetTypeName" width="100"/>
      <el-table-column label="总插槽" prop="totalSlots" width="70" align="center"/>
      <el-table-column label="空闲" prop="freeSlots" width="70" align="center"/>
      <el-table-column label="已用" prop="usedSlots" width="70" align="center"/>
      <el-table-column label="可用" prop="availableNum" width="70" align="center"/>
      <el-table-column label="状态" prop="status" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '1'" type="success">使用中</el-tag>
          <el-tag v-else-if="scope.row.status === '0'" type="info">未投放</el-tag>
          <el-tag v-else type="danger">故障</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="270">
        <template #default="scope">
          <el-button icon="View" round type="primary" @click="handleViewSlot(scope.row)">插槽</el-button>
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

    <!-- 添加或修改柜机对话框 -->
    <el-dialog v-model="open" :title="title" append-to-body width="600px">
      <el-form ref="cabinetRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="柜机编号" prop="cabinetNo">
          <el-input v-model="form.cabinetNo" placeholder="请输入柜机编号"/>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称"/>
        </el-form-item>
        <el-form-item label="柜机类型" prop="cabinetTypeId">
          <el-select
              v-model="form.cabinetTypeId"
              placeholder="请选择柜机类型"
              style="width: 100%"
          >
            <el-option
                v-for="item in cabinetTypeOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="总插槽数" prop="totalSlots">
          <el-input :model-value="autoTotalSlots" disabled placeholder="选择柜机类型后自动生成"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入内容" type="textarea"/>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
              v-model="form.status"
              placeholder="请选择状态"
              style="width: 100%"
          >
            <el-option label="未投放" value="0"/>
            <el-option label="使用中" value="1"/>
            <el-option label="故障" value="-1"/>
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

    <!-- 查看插槽对话框 -->
    <el-dialog v-model="slotOpen" title="插槽详情" append-to-body width="700px">
      <el-table v-loading="slotLoading" :data="slotList" empty-text="暂无插槽数据">
        <el-table-column label="插槽编号" prop="slotNo" width="150" align="center"/>
        <el-table-column label="充电宝编号" prop="powerBank.powerBankNo" width="180"/>
        <el-table-column label="电量" prop="powerBank.electricity" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.powerBank">{{ scope.row.powerBank.electricity }}%</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '1'" type="success">占用</el-tag>
            <el-tag v-else-if="scope.row.status === '0'" type="info">空闲</el-tag>
            <el-tag v-else type="warning">锁定</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

  </div>
</template>

<script name="Cabinet" setup>
import {
  listCabinet,
  addCabinet,
  getCabinet,
  updateCabinet,
  delCabinet
} from "@/api/device/cabinet";
import { getCabinetTypeList } from "@/api/device/cabinetType";
import {ElMessage, ElMessageBox} from "element-plus";
import {Check, Delete, View} from "@element-plus/icons-vue";

const cabinetList = ref([]);
const total = ref(0);
const loading = ref(true);

const open = ref(false);
const title = ref("");

const slotOpen = ref(false);
const slotLoading = ref(false);
const slotList = ref([]);

const ids = ref([]);
const single = ref(true);
const multiple = ref(true);

const cabinetTypeOptions = ref([]);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    cabinetNo: null,
    name: null,
    cabinetTypeId: null,
    status: null
  },
  form: {},
  rules: {
    cabinetNo: [
      { required: true, message: "柜机编号不能为空", trigger: "blur" }
    ],
    name: [
      { required: true, message: "名称不能为空", trigger: "blur" }
    ],
    cabinetTypeId: [
      { required: true, message: "请选择柜机类型", trigger: "change" }
    ],
    totalSlots: [
      { required: true, message: "请输入总插槽数", trigger: "blur" }
    ],
    status: [
      { required: true, message: "请选择状态", trigger: "change" }
    ]
  }
});

const {queryParams, form, rules} = toRefs(data);

// 根据选择的柜机类型自动计算总插槽数
const autoTotalSlots = computed(() => {
  if (!form.value.cabinetTypeId) return '';
  const selected = cabinetTypeOptions.value.find(item => item.id === form.value.cabinetTypeId);
  return selected ? selected.totalSlots : '';
});

/** 查询柜机列表 */
function getList() {
  loading.value = true;
  listCabinet(queryParams.value).then(response => {
    cabinetList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 加载柜机类型选项 */
function getCabinetTypes() {
  getCabinetTypeList().then(response => {
    cabinetTypeOptions.value = response.data;
  });
}

/** 搜索按钮操作 */
function handleQuery() {
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  queryParams.value.pageNum = 1
  queryParams.value.pageSize = 10
  queryParams.value.cabinetNo = null
  queryParams.value.name = null
  queryParams.value.cabinetTypeId = null
  queryParams.value.status = null
  handleQuery();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    cabinetNo: null,
    name: null,
    cabinetTypeId: null,
    totalSlots: null,
    description: null,
    status: null,
    remark: null
  };
}

// 新增按钮操作
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加柜机";
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
  getCabinet(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改柜机";
  });
}

// 提交按钮
function submitForm() {
  if (form.value.id != null) {
    updateCabinet(form.value).then(response => {
      ElMessage.success("修改成功");
      open.value = false;
      getList();
    });
  } else {
    addCabinet(form.value).then(response => {
      ElMessage.success("新增成功")
      open.value = false;
      getList();
    });
  }
}

// 删除按钮操作
function handleDelete(row) {
  const _ids = row.id || ids.value;
  ElMessageBox.confirm('是否确认删除柜机编号为"' + _ids + '"的数据项？', "系统提示", {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: "warning",
  }).then(function () {
    return delCabinet(_ids);
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

// 查看插槽
function handleViewSlot(row) {
  slotLoading.value = true;
  slotOpen.value = true;
  getCabinet(row.id).then(response => {
    const data = response.data;
    slotList.value = data.cabinetSlotList || [];
    slotLoading.value = false;
  }).catch(() => {
    slotLoading.value = false;
  });
}

getCabinetTypes();
getList()
</script>

<template>
  <div class="app-container">

    <!-- 搜索表单 -->
    <el-form ref="queryRef" :inline="true" label-width="68px">
      <el-form-item label="名称" prop="name">
        <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入名称"
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
            v-hasPermi="['device:cabinetType:add']"
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
    <el-table v-loading="loading" :data="cabinetTypeList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column label="名称" prop="name" width="150"/>
      <el-table-column label="总插槽数量" prop="totalSlots" width="110"/>
      <el-table-column label="描述" prop="description"/>
      <el-table-column label="状态" prop="status" width="100">
        <template #default="scope">
          <el-tag>
            {{ scope.row.status === '1' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template #default="scope">
          <el-button :icon="Check" round type="success" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button icon="Delete" round  type="danger" @click="handleDelete(scope.row)">删除</el-button>
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


    <el-dialog v-model="open" :title="title" append-to-body width="600px">
      <el-form ref="cabinetTypeRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称"/>
        </el-form-item>
        <el-form-item label="总插槽数量" prop="totalSlots">
          <el-select
              v-model="form.totalSlots"
              class="m-2"
              placeholder="请选择总插槽数量"
              style="width: 100%"
          >
            <el-option
                v-for="item in 20"
                :key="item"
                :label="item"
                :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入内容" type="textarea"/>
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

<script name="CabinetType" setup>
//引入api接口
import {
  listCabinetType,
  addCabinetType,
  getCabinetType,
  updateCabinetType,
  delCabinetType
} from "@/api/device/cabinetType";
import {ElMessage, ElMessageBox} from "element-plus";
import {Check} from "@element-plus/icons-vue";
//定义分页列表数据模型
const cabinetTypeList = ref([]);
//定义列表总记录数模型
const total = ref(0);
//加载数据时显示的动效控制模型
const loading = ref(true);

const open = ref(false);
const title = ref("");

//定义批量操作id列表模型
const ids = ref([]);
//定义单选控制模型
const single = ref(true);
//定义多选控制模型
const multiple = ref(true);

//Vue 3 中的两种响应式数据绑定方式：reactive 和 ref
//ref定义：基本数据类型，适用于简单的响应式数据
//reactive定义：对象（或数组）数据类型，则适用于复杂对象或数组的响应式数据

const data = reactive({
  //定义搜索模型
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
  },
  //封装表单数据
  form: {}
});

//toRefs 是一个Vue3中提供的API，可将一个响应式对象转换为普通对象，其中属性变成了对原始对象属性的引用
const {queryParams, form} = toRefs(data);

/** 查询柜机类型列表 */
function getList() {
  loading.value = true;
  listCabinetType(queryParams.value).then(response => {
    cabinetTypeList.value = response.rows;
    total.value = response.total;
    loading.value = false;
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
  queryParams.value.name = null
  handleQuery();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    name: null,
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
  title.value = "添加柜机类型";
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
  getCabinetType(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改柜机类型";
  });
}

// 提交按钮
function submitForm() {
  if (form.value.id != null) {
    updateCabinetType(form.value).then(response => {
      ElMessage.success("修改成功");
      open.value = false;
      getList();
    });
  } else {
    addCabinetType(form.value).then(response => {
      ElMessage.success("新增成功")
      open.value = false;
      getList();
    });
  }
}

// 删除按钮操作
function handleDelete(row) {
  const _ids = row.id || ids.value;
  ElMessageBox.confirm('是否确认删除柜机类型编号为"' + _ids + '"的数据项？', "系统提示", {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: "warning",
  }).then(function () {
    return delCabinetType(_ids);
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

//执行查询柜机类型列表
getList()
</script>
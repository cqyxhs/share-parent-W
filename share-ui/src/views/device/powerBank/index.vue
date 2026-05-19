<template>
  <div class="app-container">

    <!-- 功能按钮栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            icon="Plus"
            plain
            type="primary"
            @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            icon="Delete"
            plain
            type="danger"
            @click="handleDelete"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 搜索表单 -->
    <el-form v-show="showSearch" ref="queryRef" :inline="true" :model="queryParams" label-width="68px">
      <el-form-item label="编号" prop="powerBankNo">
        <el-input
            v-model="queryParams.powerBankNo"
            clearable
            placeholder="请输入充电宝编号"
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="Search" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据展示表格 -->
    <el-table v-loading="loading" :data="powerBankList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column label="编号" prop="powerBankNo" width="120"/>
      <el-table-column label="电量" prop="electricity" width="100">
        <template #default="scope">
          <el-tag type="success">
            {{ scope.row.electricity }}%
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述" prop="description" width="200"/>
      <el-table-column label="状态" prop="status" width="100">
        <template #default="scope">
          <el-tag>
            {{ scope.row.status === '0' ? '未投放' : scope.row.status === '1' ? '已投放' : scope.row.status === '2' ? '已租用' : scope.row.status === '3' ? '充电中' : "故障" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建人" prop="createBy" width="100" />
      <el-table-column label="创建时间" prop="createTime" width="300" />
      <el-table-column label="修改人" prop="updateBy" width="100" />
      <el-table-column label="修改时间" prop="updateTime"  />
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="200">
        <template #default="scope">
          <el-button icon="Check" round type="success" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button icon="Delete" round type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页条组件 -->
    <pagination
        v-show="total>0"
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNum"
        :total="total"
        @pagination="getList"
    />

    <!-- 添加或修改对话框 -->
    <el-dialog v-model="open" :title="title" append-to-body width="500px">
      <el-form ref="powerBankRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="编号" prop="powerBankNo">
          <el-input v-model="form.powerBankNo" placeholder="请输入充电宝编号" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入内容" type="textarea" />
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

<script setup>
import {listPowerBank, addPowerBank, getPowerBank, updatePowerBank, delPowerBank} from "@/api/device/powerBank";
import {Check} from "@element-plus/icons-vue";

defineOptions({
  name: "PowerBank"
})

const { proxy } = getCurrentInstance();

//定义分页列表数据模型
const powerBankList = ref([]);
//定义列表总记录数模型
const total = ref(0);
//加载数据时显示的动效控制模型
const loading = ref(true);
//定义隐藏搜索控制模型
const showSearch = ref(true);

//新增与修改弹出层标题模型
const title = ref("");
//新增与修改弹出层控制模型
const open = ref(false);

//定义批量操作id列表模型
const ids = ref([]);
//定义单选控制模型
const single = ref(true);
//定义多选控制模型
const multiple = ref(true);

const data = reactive({
  //定义搜索模型
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    powerBankNo: null
  },
  form: {},
  rules: {
    powerBankNo: [
      { required: true, message: "充电宝编号不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询列表 */
function getList() {
  loading.value = true;
  listPowerBank(queryParams.value).then(response => {
    powerBankList.value = response.rows;
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
  proxy.resetForm("queryRef");
  handleQuery();
}
// 表单重置
function reset() {
  form.value = {
    id: null,
    name: null
  };
  proxy.resetForm("productUnitRef");
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加充电宝";
}



// 修改按钮操作
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getPowerBank(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改商品单位";
  });
}

// 提交按钮
function submitForm() {
  proxy.$refs["powerBankRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updatePowerBank(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addPowerBank(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length !== 1;
}

// 删除按钮操作
function handleDelete(row) {
  const _ids = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除充电宝编号为"' + _ids + '"的数据项？').then(function() {
    return delPowerBank(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList()
</script>
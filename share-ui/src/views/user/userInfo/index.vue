<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :inline="true" :model="queryParams" label-width="68px">
      <el-form-item label="创建时间" style="width: 308px">
        <el-date-picker
            v-model="dateRange"
            end-placeholder="结束日期"
            range-separator="-"
            start-placeholder="开始日期"
            type="daterange"
            value-format="YYYY-MM-DD"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button icon="Search" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            v-hasPermi="['user:userInfo:export']"
            icon="Download"
            plain
            type="warning"
            @click="handleExport"
        >导出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userInfoList" @selection-change="handleSelectionChange">
      <el-table-column label="微信openId" prop="wxOpenId"/>
      <el-table-column label="会员昵称" prop="nickname" width="120"/>
      <el-table-column label="最后一次登录时间" prop="lastLoginTime" width="180"/>
      <el-table-column #default="scope" label="状态" prop="status" width="100">
        <el-tag :type="scope.row.status === 1? 'success' : 'danger'">
          {{ scope.row.status === 1 ? '正常' : '停用' }}
        </el-tag>
      </el-table-column>
      <el-table-column #default="scope" label="认证状态" prop="depositStatus" width="100">
        <el-tag :type="scope.row.depositStatus !== 0?'success' : 'danger'">
          {{ scope.row.depositStatus === 0 ? '未认证' : scope.row.depositStatus === 1 ? '免押金' : '已交押金' }}
        </el-tag>
      </el-table-column>
      <el-table-column label="注册时间" prop="createTime" width="180"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="100">
        <template #default="scope">
          <el-button v-hasPermi="['user:userInfo:query']" link type="primary" @click="handleShow(scope.row.id)">详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
        v-show="total>0"
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNum"
        :total="total"
        @pagination="getList"
    />

    <!-- 添加或修改用户对话框 -->
    <el-dialog v-model="open" :title="title" append-to-body width="70%">
      <el-form ref="userInfoRef" :model="form" label-width="140px">
        <el-divider/>
        <span style="margin-bottom: 5px;">基本信息</span>
        <el-row>
          <el-col :span="12">
            <el-form-item label="头像">
              <img :src="form.avatarUrl" width="50"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称">
              {{ form.nickname }}
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="微信标识">
              {{ form.wxOpenId }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              {{ form.sex === 1 ? '女' : '男' }}
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="最后一次登录ip">
              {{ form.lastLoginIp }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最后一次登录时间">
              {{ form.lastLoginTime }}
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-tag>
                {{ form.status === 1 ? '正常' : '停用' }}
              </el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="创建时间">
              {{ form.createTime }}
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="认证状态">
              {{ form.depositStatus == 0 ? '未认证' : form.depositStatus == 1 ? '免押金' : '已交押金' }}
            </el-form-item>
          </el-col>
          <el-col :span="12">

          </el-col>
        </el-row>

      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({
  name: "UserInfo",
});
import {listUserInfo, getUserInfo} from "@/api/user/userInfo";

const {proxy} = getCurrentInstance();

const userInfoList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
});

const {queryParams, form} = toRefs(data);

/** 查询用户列表 */
function getList() {
  loading.value = true;
  listUserInfo(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    userInfoList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 修改按钮操作 */
function handleShow(id) {
  getUserInfo(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "详情";
  });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('user/userInfo/export', {
    ...queryParams.value
  }, `userInfo_${new Date().getTime()}.xlsx`)
}

getList();
</script>
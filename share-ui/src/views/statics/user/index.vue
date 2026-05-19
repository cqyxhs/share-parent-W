<template>
  <div class="app-container">
    <el-form ref="queryRef" :inline="true" :model="queryParams" label-width="80px">
      <el-form-item label="起始年份" prop="startYear">
        <el-date-picker
          v-model="searchObj.startYear"
          placeholder="选择起始年份"
          style="width: 120px"
          type="year"
          value-format="YYYY"
        />
      </el-form-item>
      <el-form-item label="结束年份" prop="endYear">
        <el-date-picker
          v-model="searchObj.endYear"
          placeholder="选择结束年份"
          style="width: 120px"
          type="year"
          value-format="YYYY"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="Search" type="primary" @click="showChart">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <div ref="chart" style="width: 600px; height: 400px;"></div>
  </div>
</template>
<script  name="userStatics">
import { getUserCount } from "@/api/statics/statics.js";
import * as echarts from 'echarts';
export default {
  data() {
    return {
      searchObj: {
        startYear: '',
        endYear: ''
      },
      btnDisabled: false,
      chart: null,
      title: '用户注册量',
      xData: [], // x轴数据
      yData: [] // y轴数据
    }
  },
  created() {
    this.showChart()
  },
  methods: {
    // 初始化图表数据
    showChart() {
      getUserCount(this.searchObj.startYear,this.searchObj.endYear).then(response => {
        this.yData = response.data.countList
        this.xData = response.data.dateList
        this.setChartData()
      })
    },
    setChartData() {
      // 基于准备好的dom，初始化echarts实例
      var myChart = echarts.init(this.$refs.chart)
      // 指定图表的配置项和数据
      var option = {
        title: {
          text: this.title + '统计'
        },
        tooltip: {},
        legend: {
          data: [this.title]
        },
        xAxis: {
          data: this.xData
        },
        yAxis: {
          minInterval: 1
        },
        series: [{
          name: this.title,
          type: 'line',
          smooth: true,
          areaStyle: {},
          data: this.yData
        }]
      }
      // 使用刚指定的配置项和数据显示图表。
      myChart.setOption(option)
    },
    // 重置查询条件
    resetQuery() {
      this.searchObj.startYear = ''
      this.searchObj.endYear = ''
      this.showChart()
    }
  }
}
</script>
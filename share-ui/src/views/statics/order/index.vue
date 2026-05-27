<template>
  <div class="app-container">
    <el-form ref="queryRef" :inline="true" :model="searchObj" label-width="100px">
      <el-form-item label="查询类型" prop="select">
        <el-select v-model="searchObj.select" placeholder="请选择查询类型" clearable>
          <el-option label="按日统计" value="day"></el-option>
          <el-option label="按月统计" value="month"></el-option>
          <el-option label="按年统计" value="year"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button icon="Search" type="primary" @click="showChart">查询</el-button>
      </el-form-item>
    </el-form>
    <div ref="chart" style="width: 100%; height: 500px;"></div>
  </div>
</template>

<script>
defineOptions({
  name: 'orderStatics'
})
import * as echarts from 'echarts';
import { getOrderCount } from "@/api/statics/order";

export default {
  data() {
    return {
      searchObj: {
        select: ''
      },
      chart: null,
      title: '订单统计',
      xData: [], // x轴数据
      yData: [] // y轴数据
    }
  },
  mounted() {
    // 页面加载完成后初始化图表
    this.showChart();
  },
  methods: {
    // 初始化图表数据
    showChart() {
      if (!this.searchObj.select) {
        this.$message.warning("请先选择查询类型");
        return;
      }

      getOrderCount(this.searchObj.select).then(response => {
        if (response.code === 200) {
          this.yData = response.data.countList || [];
          this.xData = response.data.dateList || [];
          
          if (this.xData.length === 0) {
            this.$message.info("暂无数据");
            return;
          }
          
          this.setChartData();
        } else {
          this.$message.error(response.msg || "获取数据失败");
        }
      }).catch(error => {
        console.error('获取订单统计数据失败:', error);
        this.$message.error("获取数据失败，请稍后重试");
      });
    },

    setChartData() {
      this.$nextTick(() => {
        // 确保DOM元素已经渲染完成
        if (!this.$refs.chart) {
          console.error("图表DOM元素未找到");
          return;
        }

        // 基于准备好的dom，初始化echarts实例
        let myChart = this.chart;
        if (!myChart) {
          myChart = echarts.init(this.$refs.chart);
          this.chart = myChart;
        }

        // 指定图表的配置项和数据
        const option = {
          title: {
            text: this.title
          },
          tooltip: {
            trigger: 'axis',
            formatter: '{b}<br />{a}: {c}'
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: this.xData,
            boundaryGap: false
          },
          yAxis: {
            type: 'value',
            minInterval: 1
          },
          series: [{
            name: '订单数量',
            type: 'line',
            data: this.yData,
            smooth: true
          }]
        };

        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);

        // 处理窗口大小改变事件
        window.addEventListener('resize', () => {
          if (myChart) {
            myChart.resize();
          }
        });
      });
    },
  },
  
  beforeUnmount() {
    // 组件销毁前清理图表实例
    if (this.chart) {
      this.chart.dispose();
      this.chart = null;
    }
  }
}
</script>
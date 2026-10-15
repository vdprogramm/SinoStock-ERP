import { useEffect, useState } from "react";
import { Button, Card, Col, Row, Space, Statistic, Table, Tag, Typography, message } from "antd";
import { AreaChart, Area, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { BankOutlined, CheckCircleOutlined, ClockCircleOutlined, InboxOutlined, ReloadOutlined } from "@ant-design/icons";
import { request } from "../api";
import type { Dashboard } from "../types";
import { money } from "../types";

export default function OverviewPage(){
 const [data,setData]=useState<Dashboard|null>(null),[loading,setLoading]=useState(true);
 const load=async()=>{setLoading(true);try{setData(await request<Dashboard>("/dashboard"));}catch(e){message.error((e as Error).message);}finally{setLoading(false);}};
 useEffect(()=>{void load();},[]);
 const metrics=[{title:"Tổng sản phẩm",value:data?.totalProducts??0,icon:<InboxOutlined/>},
 {title:"Đơn hàng chờ xác nhận",value:data?.pendingOrders??0,icon:<ClockCircleOutlined/>},
 {title:"Đơn đã xác nhận",value:data?.confirmedOrders??0,icon:<CheckCircleOutlined/>},
 {title:"Doanh thu đã xác nhận",value:money(data?.totalRevenueCny),icon:<BankOutlined/>}];
 return <>
 <div className="page-head"><div><h1 className="page-title">Tổng quan doanh nghiệp</h1><p className="page-subtitle">Theo dõi tình hình kinh doanh và tồn kho theo thời gian thực.</p></div><Button icon={<ReloadOutlined/>} onClick={()=>void load()} loading={loading}>Làm mới</Button></div>
 <Row gutter={[16,16]}>{metrics.map(m=><Col xs={12} md={12} xl={6} key={m.title}><Card loading={loading}><div className="metric-icon">{m.icon}</div><Statistic title={m.title} value={m.value}/></Card></Col>)}</Row>
 <Row gutter={[16,16]} style={{marginTop:16}}>
 <Col xs={24} lg={17}><Card title="Doanh thu 7 ngày gần nhất" loading={loading}><div className="chart-wrap"><ResponsiveContainer width="100%" height="100%"><AreaChart data={data?.dailyRevenue||[]} margin={{top:10,right:12,bottom:5,left:4}}><CartesianGrid strokeDasharray="3 3" stroke="#edf1f4"/><XAxis dataKey="date" tickFormatter={v=>String(v).slice(5)}/><YAxis tickFormatter={n=>Number(n).toLocaleString("vi-VN")}/><Tooltip formatter={value=>money(Number(value))}/><Area type="monotone" dataKey="revenueCny" name="Doanh thu" stroke="#0c8075" fill="#c2e9e2" strokeWidth={2}/></AreaChart></ResponsiveContainer></div></Card></Col>
 <Col xs={24} lg={7}><Card title="Tình trạng vận hành" loading={loading}><Space direction="vertical" size="large" style={{width:"100%"}}>
 <div><Typography.Text type="secondary">Sản phẩm sắp hết hàng</Typography.Text><div className="strong-value">{data?.lowStockProducts||0} <Tag color="orange">Cần kiểm tra</Tag></div></div>
 <div><Typography.Text type="secondary">Đơn đã hủy</Typography.Text><div className="strong-value">{data?.cancelledOrders||0}</div></div>
 <Typography.Text type="secondary">Doanh thu là tổng giá trị các đơn đã xác nhận, chưa trừ chi phí hay công nợ.</Typography.Text>
 </Space></Card></Col></Row>
 <Card title="Thống kê bán hàng theo ngày" style={{marginTop:16}}><Table size="small" rowKey="date" pagination={false} dataSource={data?.dailyRevenue||[]} columns={[{title:"Ngày",dataIndex:"date"},{title:"Số đơn",dataIndex:"orders"},{title:"Doanh thu (CNY)",dataIndex:"revenueCny",render:(v:number)=>money(v)}]}/></Card>
 </>;
}

import {useEffect,useState} from "react";
import {Button,Card,Col,Row,Statistic,Table,message} from "antd";import {ReloadOutlined} from "@ant-design/icons";
import {request} from "../api";import type {ReportSummary} from "../types";import {money} from "../types";
export default function ReportsPage(){
 const [data,setData]=useState<ReportSummary|null>(null),[loading,setLoading]=useState(false);
 const load=async()=>{setLoading(true);try{setData(await request<ReportSummary>("/reports/summary"));}catch(e){message.error((e as Error).message);}finally{setLoading(false);}};
 useEffect(()=>{void load();},[]);
 const metrics=[["Doanh thu đơn xác nhận",money(data?.confirmedRevenueCny)],["Tiền đã ghi nhận thu",money(data?.recordedReceiptsCny)],["Công nợ còn lại",money(data?.pendingReceivablesCny)],["Chi phí hàng đã nhập",money(data?.receivedPurchaseCostCny)]];
 return <><div className="page-head"><div><h1 className="page-title">Báo cáo điều hành</h1><p className="page-subtitle">Tổng hợp hoạt động mua hàng, bán hàng và thu tiền.</p></div><Button icon={<ReloadOutlined/>} onClick={()=>void load()}>Làm mới</Button></div>
 <Row gutter={[16,16]}>{metrics.map(([title,value])=><Col xs={12} md={12} xl={6} key={title}><Card loading={loading}><Statistic title={title} value={value}/></Card></Col>)}</Row>
 <Card title="Quy mô hoạt động" style={{marginTop:16}}><Table pagination={false} rowKey="key" dataSource={[{key:"sales",label:"Số đơn hàng bán",count:data?.totalOrders||0},{key:"purchase",label:"Số phiếu mua hàng",count:data?.totalPurchaseOrders||0}]} columns={[{title:"Chỉ số",dataIndex:"label"},{title:"Số lượng",dataIndex:"count"}]}/><p style={{color:"#66758b"}}>Báo cáo quản trị nội bộ, chưa phải báo cáo kế toán hoặc thuế chính thức.</p></Card></>;
}

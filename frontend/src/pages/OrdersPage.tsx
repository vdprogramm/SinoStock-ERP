import {useEffect,useState} from "react";
import {Button,Card,Form,InputNumber,Modal,Popconfirm,Select,Space,Table,Tag,message} from "antd";
import {PlusOutlined,ReloadOutlined} from "@ant-design/icons";
import {request} from "../api";import type {Product,Role,SaleOrder,OrderStatus} from "../types";import {money,productName,statusNames,dateTime} from "../types";
const statusColor:Record<OrderStatus,string>={PENDING:"gold",CONFIRMED:"green",CANCELLED:"red"};
export default function OrdersPage({role}:{role:Role}){
 const [rows,setRows]=useState<SaleOrder[]>([]),[products,setProducts]=useState<Product[]>([]),[loading,setLoading]=useState(false),[open,setOpen]=useState(false),[form]=Form.useForm();
 const canWrite=["ADMIN","SALES_STAFF","STAFF"].includes(role);
 const load=async()=>{setLoading(true);try{const [a,b]=await Promise.all([request<SaleOrder[]>("/orders"),request<Product[]>("/products")]);setRows(a);setProducts(b);}catch(e){message.error((e as Error).message);}finally{setLoading(false);}};
 useEffect(()=>{void load();},[]);
 const create=async(v:{productId:number;quantity:number})=>{try{await request("/orders",{method:"POST",body:{items:[v]}});message.success("Đã tạo đơn chờ xác nhận");setOpen(false);form.resetFields();void load();}catch(e){message.error((e as Error).message);}};
 const update=async(id:number,action:"confirm"|"cancel")=>{try{await request("/orders/"+id+"/"+action,{method:"PATCH"});message.success("Đã cập nhật đơn hàng");void load();}catch(e){message.error((e as Error).message);}};
 return <><div className="page-head"><div><h1 className="page-title">Quản lý đơn bán hàng</h1><p className="page-subtitle">Xác nhận đơn sẽ trừ kho; hủy đơn đã xác nhận sẽ hoàn kho nếu chưa thu tiền.</p></div><Space><Button icon={<ReloadOutlined/>} onClick={()=>void load()}>Làm mới</Button>{canWrite&&<Button icon={<PlusOutlined/>} type="primary" onClick={()=>setOpen(true)}>Tạo đơn hàng</Button>}</Space></div>
 <Card><Table rowKey="id" loading={loading} dataSource={rows} scroll={{x:900}} pagination={{pageSize:10}} expandable={{expandedRowRender:o=><div>{o.items?.map(i=><div key={i.productId}>{productName({nameCn:i.nameCn,nameEn:i.nameCn})} · {i.quantity} × {money(i.unitPriceCny)}</div>)}</div>}} columns={[
 {title:"Mã đơn",dataIndex:"orderNumber"},{title:"Ngày tạo",dataIndex:"createdAt",render:dateTime},
 {title:"Tổng tiền",dataIndex:"totalAmountCny",render:money},{title:"Người tạo",dataIndex:"createdBy"},
 {title:"Trạng thái",dataIndex:"status",render:(s:OrderStatus)=><Tag color={statusColor[s]}>{statusNames[s]}</Tag>},
 ...(canWrite?[{title:"Thao tác",render:(_:unknown,o:SaleOrder)=><Space>{o.status==="PENDING"&&<Popconfirm title="Xác nhận và trừ kho?" onConfirm={()=>void update(o.id,"confirm")} okText="Xác nhận" cancelText="Quay lại"><Button size="small" type="primary">Xác nhận</Button></Popconfirm>}{o.status!=="CANCELLED"&&<Popconfirm title="Bạn chắc chắn muốn hủy đơn?" onConfirm={()=>void update(o.id,"cancel")} okText="Hủy đơn" cancelText="Quay lại"><Button size="small" danger>Hủy đơn</Button></Popconfirm>}</Space>}]:[])
 ]}/></Card>
 <Modal open={open} title="Tạo đơn hàng" footer={null} onCancel={()=>setOpen(false)} destroyOnClose><Form form={form} layout="vertical" onFinish={v=>void create(v)}><Form.Item name="productId" label="Sản phẩm" rules={[{required:true,message:"Chọn sản phẩm"}]}><Select showSearch optionFilterProp="label" options={products.map(p=>({value:p.id,label:productName(p)+" · "+p.sku+" (tồn "+p.stockQuantity+")"}))}/></Form.Item><Form.Item name="quantity" label="Số lượng" initialValue={1} rules={[{required:true,message:"Nhập số lượng"}]}><InputNumber min={1} precision={0} style={{width:"100%"}}/></Form.Item><Button type="primary" block htmlType="submit">Lưu đơn hàng</Button></Form></Modal>
 </>;
}

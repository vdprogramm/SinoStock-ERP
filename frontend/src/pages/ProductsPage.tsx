import { useEffect,useState } from "react";
import { Button,Card,Form,Input,InputNumber,Modal,Popconfirm,Space,Table,Tag,message } from "antd";
import { PlusOutlined,ReloadOutlined } from "@ant-design/icons";
import { request } from "../api";import type {Product,Role} from "../types";import {money,productName} from "../types";
export default function ProductsPage({role}:{role:Role}){
 const [rows,setRows]=useState<Product[]>([]),[loading,setLoading]=useState(false),[open,setOpen]=useState(false),[editing,setEditing]=useState<Product|null>(null),[stockProduct,setStockProduct]=useState<Product|null>(null);
 const [form]=Form.useForm(),[stockForm]=Form.useForm();
 const allowed=role==="ADMIN"||role==="WAREHOUSE_MANAGER";
 const load=async()=>{setLoading(true);try{setRows(await request<Product[]>("/products"));}catch(e){message.error((e as Error).message);}finally{setLoading(false);}};
 useEffect(()=>{void load();},[]);
 const save=async(v:Record<string,unknown>)=>{try{
 const dto={...v,nameCn:v.nameCn,nameEn:v.nameCn};
 if(editing)await request("/products/"+editing.id,{method:"PUT",body:{sku:dto.sku,nameCn:dto.nameCn,nameEn:dto.nameEn,priceCny:dto.priceCny,lowStockThreshold:dto.lowStockThreshold}});
 else await request("/products",{method:"POST",body:dto});
 message.success("Đã lưu sản phẩm");setOpen(false);form.resetFields();void load();
 }catch(e){message.error((e as Error).message);}};
 const adjust=async(v:{changeQuantity:number;reason:string})=>{if(v.changeQuantity===0){message.warning("Số lượng phải khác 0");return;}try{await request("/products/"+stockProduct?.id+"/stock",{method:"PATCH",body:v});message.success("Đã điều chỉnh tồn kho");setStockProduct(null);stockForm.resetFields();void load();}catch(e){message.error((e as Error).message);}};
 const remove=async(id:number)=>{try{await request("/products/"+id,{method:"DELETE"});message.success("Đã ngừng kinh doanh sản phẩm");void load();}catch(e){message.error((e as Error).message);}};
 return <><div className="page-head"><div><h1 className="page-title">Quản lý hàng hóa</h1><p className="page-subtitle">Theo dõi sản phẩm, giá bán và lượng hàng tồn kho.</p></div><Space><Button icon={<ReloadOutlined/>} onClick={()=>void load()}>Làm mới</Button>{allowed&&<Button type="primary" icon={<PlusOutlined/>} onClick={()=>{setEditing(null);form.resetFields();setOpen(true);}}>Thêm sản phẩm</Button>}</Space></div>
 <Card><Table rowKey="id" loading={loading} dataSource={rows} scroll={{x:780}} pagination={{pageSize:10}} columns={[
 {title:"Mã SKU",dataIndex:"sku"}, {title:"Sản phẩm",render:(_,p)=>productName(p)},
 {title:"Giá bán",dataIndex:"priceCny",render:(n:number)=>money(n)},
 {title:"Tồn kho",dataIndex:"stockQuantity",render:(n:number,p:Product)=><Space>{n}{n<=p.lowStockThreshold&&<Tag color="orange">Sắp hết</Tag>}</Space>},
 {title:"Ngưỡng cảnh báo",dataIndex:"lowStockThreshold"},
 ...(allowed?[{title:"Thao tác",key:"actions",render:(_:unknown,p:Product)=><Space wrap><Button size="small" onClick={()=>{setEditing(p);form.setFieldsValue({...p,nameCn:productName(p)});setOpen(true);}}>Sửa</Button><Button size="small" onClick={()=>{setStockProduct(p);stockForm.resetFields();}}>Điều chỉnh kho</Button><Popconfirm title="Ngừng kinh doanh sản phẩm?" onConfirm={()=>void remove(p.id)} okText="Xác nhận" cancelText="Quay lại"><Button size="small" danger>Ngừng bán</Button></Popconfirm></Space>}]:[])
 ]}/></Card>
 <Modal title={editing?"Cập nhật sản phẩm":"Thêm sản phẩm"} open={open} footer={null} onCancel={()=>setOpen(false)} destroyOnClose><Form form={form} layout="vertical" onFinish={v=>void save(v)}>
 <Form.Item name="sku" label="Mã SKU" rules={[{required:true,message:"Vui lòng nhập mã SKU"}]}><Input/></Form.Item>
 <Form.Item name="nameCn" label="Tên sản phẩm" rules={[{required:true,message:"Vui lòng nhập tên sản phẩm"}]}><Input/></Form.Item>
 <Form.Item name="priceCny" label="Giá bán (CNY)" rules={[{required:true,message:"Vui lòng nhập giá"}]}><InputNumber min={0.01} precision={2} style={{width:"100%"}}/></Form.Item>
 {!editing&&<Form.Item name="stockQuantity" label="Tồn kho ban đầu" initialValue={0} rules={[{required:true,message:"Vui lòng nhập số lượng"}]}><InputNumber min={0} precision={0} style={{width:"100%"}}/></Form.Item>}
 <Form.Item name="lowStockThreshold" label="Ngưỡng cảnh báo" initialValue={5} rules={[{required:true,message:"Vui lòng nhập ngưỡng"}]}><InputNumber min={0} precision={0} style={{width:"100%"}}/></Form.Item>
 <Button htmlType="submit" type="primary" block>Lưu sản phẩm</Button></Form></Modal>
 <Modal title={"Điều chỉnh tồn kho: "+(stockProduct?productName(stockProduct):"")} open={!!stockProduct} footer={null} onCancel={()=>setStockProduct(null)} destroyOnClose><Form form={stockForm} onFinish={v=>void adjust(v)} layout="vertical"><Form.Item label="Số lượng thay đổi (+ nhập / - xuất)" name="changeQuantity" rules={[{required:true,message:"Nhập số lượng điều chỉnh"}]}><InputNumber precision={0} style={{width:"100%"}}/></Form.Item><Form.Item label="Lý do (tối đa 23 ký tự)" name="reason" rules={[{required:true,max:23,message:"Vui lòng nhập lý do tối đa 23 ký tự"}]}><Input maxLength={23}/></Form.Item><Button block type="primary" htmlType="submit">Xác nhận điều chỉnh</Button></Form></Modal>
 </>;
}

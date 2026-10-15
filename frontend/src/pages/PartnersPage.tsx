import {useEffect,useState} from "react";
import {Button,Card,Form,Input,Modal,Space,Table,message} from "antd";
import {PlusOutlined,ReloadOutlined} from "@ant-design/icons";
import {request} from "../api";import type {Partner} from "../types";
export default function PartnersPage({type}:{type:"customers"|"suppliers"}){
 const [items,setItems]=useState<Partner[]>([]),[loading,setLoading]=useState(false),[open,setOpen]=useState(false),[form]=Form.useForm();
 const singular=type==="customers"?"khách hàng":"nhà cung cấp";
 const load=async()=>{setLoading(true);try{setItems(await request<Partner[]>("/"+type));}catch(e){message.error((e as Error).message);}finally{setLoading(false);}};
 useEffect(()=>{void load();},[type]);
 const save=async(v:Omit<Partner,"id">)=>{try{await request("/"+type,{method:"POST",body:v});message.success("Đã lưu "+singular);form.resetFields();setOpen(false);void load();}catch(e){message.error((e as Error).message);}};
 return <><div className="page-head"><div><h1 className="page-title">Quản lý {type==="customers"?"khách hàng":"nhà cung cấp"}</h1><p className="page-subtitle">Lưu trữ thông tin liên hệ và đối tác kinh doanh.</p></div><Space><Button icon={<ReloadOutlined/>} onClick={()=>void load()}>Làm mới</Button><Button type="primary" icon={<PlusOutlined/>} onClick={()=>setOpen(true)}>Thêm {singular}</Button></Space></div><Card><Table rowKey="id" loading={loading} dataSource={items} pagination={{pageSize:10}} scroll={{x:650}} columns={[{title:"Tên",dataIndex:"name"},{title:"Số điện thoại",dataIndex:"phone",render:(v:string)=>v||"—"},{title:"Email",dataIndex:"email",render:(v:string)=>v||"—"},{title:"Địa chỉ",dataIndex:"address",render:(v:string)=>v||"—"}]}/></Card>
 <Modal open={open} title={"Thêm "+singular} onCancel={()=>setOpen(false)} footer={null} destroyOnClose><Form layout="vertical" form={form} onFinish={v=>void save(v)}><Form.Item label="Tên" name="name" rules={[{required:true,message:"Vui lòng nhập tên"}]}><Input/></Form.Item><Form.Item label="Điện thoại" name="phone"><Input/></Form.Item><Form.Item label="Email" name="email" rules={[{type:"email",message:"Email không hợp lệ"}]}><Input/></Form.Item><Form.Item label="Địa chỉ" name="address"><Input/></Form.Item><Button block htmlType="submit" type="primary">Lưu thông tin</Button></Form></Modal></>;
}

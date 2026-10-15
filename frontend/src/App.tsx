import {useState} from "react";
import { App as AntApp, Avatar, Button, Card, Form, Input, Layout, Menu, Space, Tag, Typography } from "antd";
import { ArrowLeftOutlined, BankOutlined, BarChartOutlined, DashboardOutlined, FileDoneOutlined, InboxOutlined, LogoutOutlined, MenuFoldOutlined, MenuUnfoldOutlined, ShopOutlined, ShoppingCartOutlined, TeamOutlined, TruckOutlined, UserOutlined, UserSwitchOutlined } from "@ant-design/icons";
import { Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import {useAuth} from "./auth";
import type {Role} from "./types";import {roleNames} from "./types";
import OverviewPage from "./pages/OverviewPage";
import ProductsPage from "./pages/ProductsPage";
import OrdersPage from "./pages/OrdersPage";
import PartnersPage from "./pages/PartnersPage";
import PurchasingPage from "./pages/PurchasingPage";
import AccountingPage from "./pages/AccountingPage";
import ReportsPage from "./pages/ReportsPage";
import UsersPage from "./pages/UsersPage";
import "./styles.css";

const { Sider, Header, Content } = Layout;
interface Item {path:string;label:string;icon:React.ReactNode;roles:Role[]}
const all:Role[]=["ADMIN","WAREHOUSE_MANAGER","SALES_STAFF","ACCOUNTANT","PURCHASING_MANAGER","GENERAL_MANAGER","STAFF"];
const items:Item[]=[
 {path:"overview",label:"Tổng quan",icon:<DashboardOutlined/>,roles:all},
 {path:"products",label:"Hàng hóa và kho",icon:<InboxOutlined/>,roles:all},
 {path:"orders",label:"Bán hàng",icon:<ShoppingCartOutlined/>,roles:["ADMIN","SALES_STAFF","STAFF","ACCOUNTANT","GENERAL_MANAGER"]},
 {path:"customers",label:"Khách hàng",icon:<TeamOutlined/>,roles:["ADMIN","SALES_STAFF","GENERAL_MANAGER"]},
 {path:"suppliers",label:"Nhà cung cấp",icon:<ShopOutlined/>,roles:["ADMIN","PURCHASING_MANAGER"]},
 {path:"purchasing",label:"Mua hàng và nhập kho",icon:<TruckOutlined/>,roles:["ADMIN","PURCHASING_MANAGER"]},
 {path:"accounting",label:"Kế toán và công nợ",icon:<BankOutlined/>,roles:["ADMIN","ACCOUNTANT","GENERAL_MANAGER"]},
 {path:"reports",label:"Báo cáo điều hành",icon:<BarChartOutlined/>,roles:["ADMIN","GENERAL_MANAGER","ACCOUNTANT"]},
 {path:"users",label:"Nhân sự và phân quyền",icon:<UserSwitchOutlined/>,roles:["ADMIN"]}
];
function Login(){
 const {login}=useAuth(),{message}=AntApp.useApp();
 const [loading,setLoading]=useState(false);
 const submit=async(v:{username:string;password:string})=>{setLoading(true);try{await login(v.username,v.password);message.success("Đăng nhập thành công");}catch(e){message.error((e as Error).message);}finally{setLoading(false);}};
 return <div className="login-screen"><section className="login-intro"><Tag color="cyan" style={{width:"fit-content"}}>HỆ THỐNG QUẢN TRỊ DOANH NGHIỆP</Tag><h1>SinoStock ERP</h1><p>Quản lý hoạt động kinh doanh trên một nền tảng: hàng hóa, đơn hàng, nhà cung cấp, mua hàng và kế toán.</p><Space size="large"><span>Quản lý tồn kho</span><span>6 vai trò phân quyền</span></Space></section>
 <section className="login-panel"><div className="login-box"><div style={{color:"#0c8075",fontWeight:800,fontSize:22}}>SinoStock ERP</div><h2>Chào mừng trở lại</h2><p>Đăng nhập bằng tài khoản nhân viên của bạn</p><Form layout="vertical" size="large" initialValues={{username:"admin"}} onFinish={v=>void submit(v)}><Form.Item name="username" label="Tên đăng nhập" rules={[{required:true,message:"Vui lòng nhập tên đăng nhập"}]}><Input prefix={<UserOutlined/>} placeholder="Tên đăng nhập" autoComplete="username"/></Form.Item><Form.Item name="password" label="Mật khẩu" rules={[{required:true,message:"Vui lòng nhập mật khẩu"}]}><Input.Password placeholder="Mật khẩu" autoComplete="current-password"/></Form.Item><Button type="primary" htmlType="submit" block loading={loading}>Đăng nhập</Button></Form><p style={{fontSize:12,marginTop:26}}>Chỉ dành cho người dùng được cấp quyền. Dữ liệu được lưu trên PostgreSQL.</p></div></section></div>;
}
function Workspace(){
 const {session,logout}=useAuth();const [collapsed,setCollapsed]=useState(false),location=useLocation(),navigate=useNavigate();
 if(!session)return <Navigate to="/login" replace/>;
 const visible=items.filter(i=>i.roles.includes(session.role));
 const selected=location.pathname.replace(/^\//,"").split("/")[0];
 const page=visible.find(i=>i.path===selected);
 return <Layout className="app-shell">
 <Sider width={244} collapsedWidth={72} collapsed={collapsed} breakpoint="lg" onBreakpoint={isMobile=>setCollapsed(isMobile)} style={{background:"#10243b"}}>
 <div className="sidebar-brand">{collapsed?"S":<>Sino<span>Stock</span></>}</div>
 {!collapsed&&<div className="sidebar-caption">QUẢN TRỊ DOANH NGHIỆP</div>}
 <Menu theme="dark" mode="inline" style={{background:"transparent",border:0}} selectedKeys={page?[page.path]:["overview"]}
 items={visible.map(i=>({key:i.path,icon:i.icon,label:i.label}))} onClick={e=>navigate("/"+e.key)}/>
 <div className="sidebar-foot">{!collapsed&&"Java Spring Boot · React"}</div>
 </Sider>
 <Layout>
 <Header style={{display:"flex",alignItems:"center",justifyContent:"space-between",height:72,padding:"0 26px",borderBottom:"1px solid #e8edf3"}}>
 <Space><Button type="text" icon={collapsed?<MenuUnfoldOutlined/>:<MenuFoldOutlined/>} onClick={()=>setCollapsed(!collapsed)}/><Typography.Text type="secondary">Hệ thống quản trị SinoStock</Typography.Text></Space>
 <Space size="middle"><Avatar icon={<UserOutlined/>} style={{background:"#0c8075"}}/><div className="header-user"><strong style={{display:"block",color:"#243850"}}>{session.username}</strong>{roleNames[session.role]}</div><Button onClick={logout} icon={<LogoutOutlined/>} type="text" title="Đăng xuất"/></Space>
 </Header>
 <Content className="app-content" style={{padding:28}}>
 {page?<Routes>
 <Route path="/overview" element={<OverviewPage/>}/>
 <Route path="/products" element={<ProductsPage role={session.role}/>}/>
 <Route path="/orders" element={<OrdersPage role={session.role}/>}/>
 <Route path="/customers" element={<PartnersPage type="customers"/>}/>
 <Route path="/suppliers" element={<PartnersPage type="suppliers"/>}/>
 <Route path="/purchasing" element={<PurchasingPage/>}/>
 <Route path="/accounting" element={<AccountingPage role={session.role}/>}/>
 <Route path="/reports" element={<ReportsPage/>}/>
 <Route path="/users" element={<UsersPage/>}/>
 </Routes>:<Card><FileDoneOutlined/> Bạn không có quyền truy cập mục này. <Button icon={<ArrowLeftOutlined/>} onClick={()=>navigate("/overview")}>Về tổng quan</Button></Card>}
 </Content></Layout></Layout>;
}
export default function App(){
 const {session}=useAuth();
 return <Routes><Route path="/login" element={session?<Navigate to="/overview" replace/>:<Login/>}/><Route path="/" element={<Navigate to={session?"/overview":"/login"} replace/>}/><Route path="/*" element={session?<Workspace/>:<Navigate to="/login" replace/>}/></Routes>;
}

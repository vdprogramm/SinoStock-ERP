export type Role = "ADMIN" | "WAREHOUSE_MANAGER" | "SALES_STAFF" | "ACCOUNTANT" | "PURCHASING_MANAGER" | "GENERAL_MANAGER" | "STAFF";
export type OrderStatus = "PENDING" | "CONFIRMED" | "CANCELLED";
export type PurchaseStatus = "DRAFT" | "RECEIVED" | "CANCELLED";
export interface AuthSession { token: string; username: string; role: Role; }
export interface Product { id: number; sku: string; nameCn: string; nameEn: string; priceCny: number; stockQuantity: number; lowStockThreshold: number; active: boolean; }
export interface OrderItem { productId: number; sku: string; nameCn: string; quantity: number; unitPriceCny: number; lineTotalCny: number; }
export interface SaleOrder { id: number; orderNumber: string; status: OrderStatus; totalAmountCny: number; createdBy: string; createdAt: string; confirmedAt: string | null; items: OrderItem[]; }
export interface DayRevenue { date: string; orders: number; revenueCny: number; }
export interface Dashboard { totalProducts: number; lowStockProducts: number; pendingOrders: number; confirmedOrders: number; cancelledOrders: number; totalRevenueCny: number; dailyRevenue: DayRevenue[]; }
export interface Partner { id: number; name: string; phone: string | null; email: string | null; address: string | null; }
export interface Purchase { id: number; reference: string; supplier: string; sku: string; quantity: number; unitCostCny: number; status: PurchaseStatus; createdAt: string; }
export interface Payment { id: number; orderNumber: string; amountCny: number; method: string; createdAt: string; }
export interface Receivable { orderId: number; orderNumber: string; total: number; paid: number; outstanding: number; }
export interface ReportSummary { confirmedRevenueCny: number; recordedReceiptsCny: number; receivedPurchaseCostCny: number; pendingReceivablesCny: number; totalOrders: number; totalPurchaseOrders: number; }
export interface UserView { id: number; username: string; role: Role; }
export const roleNames: Record<Role,string> = {
  ADMIN: "Quản trị viên", WAREHOUSE_MANAGER:"Quản lý kho", SALES_STAFF:"Nhân viên bán hàng",
  ACCOUNTANT:"Kế toán", PURCHASING_MANAGER:"Quản lý mua hàng", GENERAL_MANAGER:"Ban giám đốc",
  STAFF:"Nhân viên (cũ)"
};
export const statusNames: Record<OrderStatus | PurchaseStatus,string> = {
  PENDING:"Chờ xác nhận", CONFIRMED:"Đã xác nhận", CANCELLED:"Đã hủy", DRAFT:"Chờ nhập kho", RECEIVED:"Đã nhập kho"
};
export const money = (value: number | string | undefined | null): string =>
  (Number(value ?? 0)).toLocaleString("vi-VN",{minimumFractionDigits:2,maximumFractionDigits:2})+" ¥";
export const dateTime = (value: string | null | undefined): string =>
  value ? new Date(value).toLocaleString("vi-VN") : "—";
const sampleNames:Record<string,string> = {"无线鼠标":"Chuột không dây","机械键盘":"Bàn phím cơ","USB-C数据线":"Cáp USB-C"};
export const productName = (p:Pick<Product,"nameCn"|"nameEn">):string => sampleNames[p.nameCn] || p.nameCn || p.nameEn;

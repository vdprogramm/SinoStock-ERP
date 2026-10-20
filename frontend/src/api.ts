import type { AuthSession } from "./types";

const SESSION_KEY = "sinostock_react_session";
export const getSavedSession = ():AuthSession | null => {
  try { return JSON.parse(sessionStorage.getItem(SESSION_KEY) || "null") as AuthSession|null; }
  catch { return null; }
};
export const saveSession = (session:AuthSession|null) => {
  if(session) sessionStorage.setItem(SESSION_KEY,JSON.stringify(session));
  else sessionStorage.removeItem(SESSION_KEY);
};
export class HttpError extends Error {
  constructor(public status:number, text:string){super(text);this.name="HttpError";}
}
export async function request<T>(path:string, options:{ method?:string; body?:unknown; authorized?:boolean }={}):Promise<T> {
  const session=getSavedSession();
  const headers:Record<string,string>={"Content-Type":"application/json"};
  if(options.authorized!==false && session?.token) headers.Authorization="Bearer "+session.token;
  let response:Response;
  try {
    response=await fetch("/api"+path,{
      method:options.method||"GET", headers,
      ...(options.body!==undefined?{body:JSON.stringify(options.body)}:{}),
    });
  } catch { throw new Error("Không thể kết nối máy chủ. Vui lòng kiểm tra mạng và thử lại."); }
  const data=await response.json().catch(()=>null) as {message?:string}|null;
  if(!response.ok){
    if([502,503,504].includes(response.status)) throw new HttpError(response.status,"Máy chủ đang khởi động sau thời gian nghỉ. Vui lòng đợi khoảng một phút rồi thử lại.");
    if(response.status===401 && path!=="/auth/login"){
      saveSession(null);
      window.dispatchEvent(new Event("sinostock:expired"));
    }
    throw new HttpError(response.status,data?.message||(
      response.status===403?"Bạn không có quyền thực hiện thao tác này.":
      response.status===401?"Phiên đăng nhập đã hết hạn.": "Yêu cầu thất bại (mã "+response.status+")."
    ));
  }
  return data as T;
}

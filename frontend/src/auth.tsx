import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { getSavedSession, request, saveSession } from "./api";
import type { AuthSession } from "./types";

interface AuthValue {
  session:AuthSession|null;
  login:(username:string,password:string)=>Promise<void>;
  logout:()=>void;
}
const Context=createContext<AuthValue|undefined>(undefined);
export function AuthProvider({children}:{children:ReactNode}){
  const [session,setSession]=useState<AuthSession|null>(()=>getSavedSession());
  const login=useCallback(async(username:string,password:string)=>{
    const response=await request<AuthSession>("/auth/login",{method:"POST",body:{username,password},authorized:false});
    const current={token:response.token,username:response.username,role:response.role};
    saveSession(current);setSession(current);
  },[]);
  const logout=useCallback(()=>{saveSession(null);setSession(null);},[]);
  useEffect(()=>{
    const expire=()=>setSession(null);
    window.addEventListener("sinostock:expired",expire);
    return ()=>window.removeEventListener("sinostock:expired",expire);
  },[]);
  const value=useMemo(()=>({session,login,logout}),[session,login,logout]);
  return <Context.Provider value={value}>{children}</Context.Provider>;
}
export function useAuth(){const value=useContext(Context);if(!value)throw Error("Thiếu AuthProvider");return value;}

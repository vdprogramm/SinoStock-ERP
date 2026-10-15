import React from "react";
import ReactDOM from "react-dom/client";
import { App as AntApp, ConfigProvider } from "antd";
import viVN from "antd/locale/vi_VN";
import { HashRouter } from "react-router-dom";
import { AuthProvider } from "./auth";
import App from "./App";
import "./styles.css";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <ConfigProvider locale={viVN} theme={{token:{colorPrimary:"#0c8075",borderRadius:10,fontFamily:'Inter, "Segoe UI", Arial, sans-serif'},components:{Layout:{siderBg:"#10243b",headerBg:"#ffffff"}}}}>
      <AntApp><AuthProvider><HashRouter><App/></HashRouter></AuthProvider></AntApp>
    </ConfigProvider>
  </React.StrictMode>
);

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.bundle.js'

import App from './App.vue'
// 기본 파일명이 index.js인 경우 디렉터리명만으로 import가 가능하다/
import router from './router'

const app = createApp(App)

// import한 createPinia() 함수를 통해서 pinia 객체를 생성하고 애플리케이션 인스턴스에 등록한다.
app.use(createPinia())
// import한 router 객체를 어플리케이션 인스턴스에 등록한다.
app.use(router)

app.mount('#app')

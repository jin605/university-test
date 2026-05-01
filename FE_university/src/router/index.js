// import AuthLayout from '@/layout/AuthLayout.vue'
// import BaseLayout from '@/layout/BaseLayout.vue'
// import NotFound from '@/views/common/NotFound.vue'
// import AddDepartment from '@/views/department/AddDepartment.vue'
// import DepartmentDetail from '@/views/department/DepartmentDetail.vue'
// import Departments from '@/views/department/Departments.vue'
// import Home from '@/views/Home.vue'
// import Login from '@/views/auth/Login.vue'
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

// 지연 로딩(Lazy Loading)
// 지연 로딩은 컴포넌트가 이용되는 시점에 컴포넌트 및 관련된 모듈을 웹 서버로부터 로딩하는 방법니다.
const Home = () => import('@/views/Home.vue')
const Departments = () => import('@/views/department/Departments.vue')
const DepartmentDetail = () => import('@/views/department/DepartmentDetail.vue')
const Login = () => import('@/views/auth/Login.vue')
const AddDepartment = () => import('@/views/department/AddDepartment.vue')
const NotFound = () => import('@/views/common/NotFound.vue')
const AuthLayout = () => import('@/layout/AuthLayout.vue')
const BaseLayout = () => import('@/layout/BaseLayout.vue')
// router 객체를 생성하기 위해서는 vue-router에서 제공하는 createRouter 함수를 사용한다.
const router = createRouter({
  // 라우터가 사용할 라우팅 모드 지정 (html5 모드)
  history: createWebHistory(import.meta.env.BASE_URL),
  // 요청 경로에 따라 렌더링할 컴포넌트를 배열로 지정한다.
  // 명명된 라우트
  // routes: [
  //   {name: 'home', path: '/', component: Home},
  //   {name: 'departments', path: '/departments', component:Departments},
  //   // 동적 라우트는 일정한 패턴의 URI 경로에 하나의 라우트에 연결하는 방법이다.
  //   {name: 'departments/no', path: '/departments/:no', component:DepartmentDetail},
  //   {name: 'departments/add', path: '/departments/add', component:AddDepartment},
  //   // 404 라운트
  //   {name: 'notfound', path: '/:paths(.*)*', component:NotFound}
  // ],
  // 중첩된 라우트
  // RouterView에 의해서 랜더링된 컴포넌트가 다시 RouterView를 이용헤서 자식 라우트에 매칭된 컴포넌트를 랜더링한다.

  routes: [
    {
      name: 'main',
      path: '/',
      component: BaseLayout,
      children: [
        {name: 'home', path: '', component: Home},
        {name: 'departments', path: 'departments', component: Departments},
        {name: 'departments/no', path: 'departments/:no', component: DepartmentDetail},
        {name: 'departments/add', path: 'departments/add', component: AddDepartment}
      ]
    },
    {
      name: 'auth', 
      path: '/auth', 
      component:AuthLayout, 
      children: 
      [{
        name: 'login', 
        path: 'login', 
        component:Login
      }]},

    // 404 라운트
    {
      name: 'notfound', 
      path: '/:paths(.*)*', 
      component:NotFound
    }

  ],
})

// 네비게이션 가드(Navigation Guard)
//  - 라이팅이 일어날 때 프로그래밍 방식으로 네이게이션을 안전하게 보호하는 기능을 수행한다.
//  - beforeEach에 전달하는 인자 중 to는 이동하려는 경로를 나타내고 from은 이동 전 현재 경로 정보이다.
router.beforeEach(async(to, from) => {
  //  - 라우팅 경로 변경, 새로고침 시에 리프레시 토큰으로 액세스 토큰을 재발급 받는다.
  const authStore = useAuthStore()

  try{

    // 액세스 토큰이 없는 경우 리프레시토큰으로 엑세스 토큰 재발급
    if (authStore.tokenInfo.accessToken === '') {
      await authStore.refreshAccessToken();
    }
    console.log(authStore.tokenInfo.accessToken);
    
    // 인증된 사용자가 로그인 페이지 요청 시 홈으로 리다이렉트
    if (to.name === 'login' && authStore.tokenInfo.accessToken) {
      return {name: 'home'}
    }
  } catch(error) {
    // 액세스 토큰 재발급 중에 에러 발생시 로그인 페이지로 리다이렉트
    if (to.name !== 'login') {
      return {name: 'login'}
    }
  }
});

export default router

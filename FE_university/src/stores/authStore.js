import apiClient from "@/api";
import { defineStore } from "pinia";
import { reactive } from "vue";


export const useAuthStore = defineStore('auth', () =>{
    // 사용자 정보
    const tokenInfo = reactive({
        accessToken: '',
        type: '',
        username: '',
        authorities: [],
        issuedAt: 0,
        expiredAt: 0
    });

    const getFirstTokenInfo = (data) => {
        const raw = data?.item ?? data?.items;

        if (Array.isArray(raw)) return raw[0] ?? null;
        if (raw && typeof raw === 'object') return raw;
        return null;
    };

    // 로그인 처리
    const login = async (formData) => {
        const response = await apiClient.post(
            '/api/v1/auth/login',
            formData,
            {
                // 브라우저가 서버로 요청을 보낼 때 쿠키, 인증 헤더 등을 함께 포함하도록 허용하는 설정
                withCredentials: true,
                _skipInterceptor: true
            }
        );

        console.log(response);

        if (response.status === 200) {
            const nextTokenInfo = getFirstTokenInfo(response.data);
            if (nextTokenInfo) Object.assign(tokenInfo, nextTokenInfo);
        }

        return response.data;
    };

    // 액세스 토큰 재발급
    const refreshAccessToken = async() => {
        const response = await apiClient.post(
            '/api/v1/auth/refresh',
            null,
            {
                withCredentials: true,
                _skipInterceptor: true
            }
        );

        if (response.status === 200) {
            const nextTokenInfo = getFirstTokenInfo(response.data);
            if (nextTokenInfo) Object.assign(tokenInfo, nextTokenInfo);
            
        }
    };

    const logout = async () => {
        const response = await apiClient.post(
            '/api/v1/auth/logout',
            null,
            {
                withCredentials: true
            }
        );

        if (response.status === 204) {
            // 사용자 정보 초기화
            clearState();

        }

        return response;
        
    };

    const clearState = () => {
        // 사용자 정보 초기화
        tokenInfo.accessToken = '';
        tokenInfo.type = '';
        tokenInfo.username =  '';
        tokenInfo.authorities =  [];
        tokenInfo.issuedAt = 0;
        tokenInfo.expiredAt = 0;
    }

    return {tokenInfo, login, refreshAccessToken, logout, clearState};
    
});


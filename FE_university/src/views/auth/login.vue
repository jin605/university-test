<template>
    <main class="form-signin w-100 m-auto">
        <LoginForm @form-submit="formSubmit"></LoginForm>
    </main>
</template>

<script setup>
    import '@/assets/css/auth/sign-in.css'
    import LoginForm from '@/components/forms/LoginForm.vue';
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'vue-router';

    const authStore = useAuthStore();
    const router = useRouter();

    const formSubmit = async (formData) => {

        try {
                const result = await authStore.login(formData);

                if (result.code === 200) {
                    router.push({name: 'home'});

                }
        } catch (error) {
            const {status, message} = error.response.data;

            if (status === 'BAD_REQUEST') {
        alert(message);
        } else if (status === 'INTERNAL_SERVER_ERROR') {
        alert('에러가 발생하였습니다.');
        }   

            
        }
    };
</script>

<style scoped>

</style>
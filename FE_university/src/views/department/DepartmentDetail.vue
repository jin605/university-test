<!-- 04_university/src/views/department/DepartmentDetail.vue -->
<template>
    <main>
        <h2>학과 상세 조회</h2>

        <DepartmentForm

            :form-type="FORM_TYPE.EDIT"
            :init-form-data="departmentStore.departments"
            @form-submit="formSubmit"/>
        
    </main>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router';
import { FORM_TYPE } from '@/constants/formType';
import DepartmentForm from '@/components/forms/DepartmentForm.vue';
import { useDepartmentStore } from '@/stores/departmentStore';
import { onMounted } from 'vue';

const router = useRouter();
const currentRoute = useRoute();
const departmentStore = useDepartmentStore();

const formSubmit = async (formData) => {

    try {
        const response = 
            await departmentStore.editDepartment(currentRoute.params.no, formData);
;

        if (response.code === 200) {

            alert('정상적으로 수정되었습니다.')
        }
        
    } catch (error) {

        const { status, message } = error.response.data;

        if (status === 'DEPARTMENT_NOT_FOUND') {
            alert(message);
        } else if (status === 'BAD_REQUEST') {
            alert('학과 정보를 모두 입력해 주세요.');
        } else if (status === 'REFRESH_TOKEN_INVALID') {
            router.push({name: 'login'});
        } else if (status === 'INTERNAL_SERVER_ERROR') {
            alert('에러가 발생하였습니다.');
        }
        
    }
}

onMounted(async() => {

    try {
        await departmentStore.fetchDepartment(currentRoute.params.no)
        
    } catch (error) {
        
        const {status, message} = error.response.data;

        if (status === 'DEPARTMENT_NOT_FOUND') {
            alert(message);
        } else if (status === 'REFRESH_TOKEN_INVALID') {
            router.push({name: 'login'});
        } else if (status === 'INTERNAL_SERVER_ERROR') {
            alert('에러가 발생하였습니다.');
        }
    }

})
</script>

<style scoped>

</style>

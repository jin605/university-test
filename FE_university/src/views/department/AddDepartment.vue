<template>
    <main>
        <DepartmentForm
            :form-type="FORM_TYPE.ADD"
            :init-form-data="initFormData"
            @form-submit="formSubmit"/>
    </main>

</template>

<script setup>
    import DepartmentForm from '@/components/forms/DepartmentForm.vue';
    import { FORM_TYPE } from '@/constants/formType';
    import { useDepartmentStore } from '@/stores/departmentStore';
    import { reactive } from 'vue';
    import { useRouter } from 'vue-router';

    const initFormData = reactive({
        name: '',
        category: '',
        openYn: 'N',
        capacity: 30
    });
    
    const departmentStore = useDepartmentStore();
    const router = useRouter();

    const formSubmit = async (formData) => {
        try {
            const response = await departmentStore.addDepartment(formData);

            if (response.code === 201) {
                alert('정상적으로 등록되었습니다.')

                router.push({name: 'departments/no', params: {no: response.item[0].no}})
            }
            
        } catch (error) {

            const { status, message } = error.response.data;

            if (status === 'BAD_REQUEST') {
                alert(message);
            } else if (status === 'REFRESH_TOKEN_INVALID') {
                router.push({name: 'login'});
            } else if (status === 'INTERNAL_SERVER_ERROR') {
                alert('에러가 발생하였습니다.');
            }

            
            
        }
    }

</script>

<style scoped>

</style>
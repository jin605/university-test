<template>
    <main>
        <select class="form-select w-auto" v-model="listLimit">
            <option value="5">5개</option>
            <option value="10">10개</option>
            <option value="20">20개</option>
        </select>
        <DepartmentTable :departments="departmentStore.departments"
        @itme-click="itmeClick"
        @delete-click="deleteClick"/>
        <Pagination
            :current-page="currentPage"
            :page-numbers="pageNumbers"
            @change-page="changePage"/>
    </main>
</template>

<script setup>
    import { onMounted, watch } from 'vue';
    import DepartmentTable from '@/components/tables/DepartmentTable.vue';
    import Pagination from '@/components/common/Pagination.vue';
    import { useDepartmentStore } from '@/stores/departmentStore';
    import { usePagination } from '@/composables/usePagination';
    import { onBeforeRouteLeave, useRouter } from 'vue-router';

    const departmentStore = useDepartmentStore();
    const { currentPage, listLimit, totalPages, pageNumbers, setCurrentPage, setTotalCount } = usePagination();
    const router = useRouter();
    
    const changePage = (page) => {

        if (page >= 1 && page <= totalPages.value) {

            setCurrentPage(page);
        }
    }

    const itmeClick = (no) => {
        router.push({name: 'departments/no', params: {no}})
    }

    onMounted(async () => {
        try {
            setCurrentPage(1);

            const response = await departmentStore.fetchDepartments(currentPage.value, listLimit.value);

            setTotalCount(response.totalCount);
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
    });

    const deleteClick = async (no) =>{
        try {
            if (confirm('정말로 삭제하시겠습니까?')) {
                const response = await departmentStore.deleteDepartment(no);
                
                if (response.code === 200) {
                    alert('정상적으로 삭제되었습니다.')

                    await departmentStore.fetchDepartments(currentPage.value, listLimit.value);
                }
            }
            
        } catch (error) {

        const { status, message } = error.response.data;

        if (status === 'DEPARTMENT_NOT_FOUND') {
            alert(message);
            
            router.push({name: 'deaprtments'});
        } else if (status === 'FORBIDDEN') {
            alert('권한이 없는 사용자입니다.');
        } else if (status === 'REFRESH_TOKEN_INVALID') {
            router.push({name: 'login'});
        } else if (status === 'INTERNAL_SERVER_ERROR') {
            alert('에러가 발생하였습니다.');
        }


            
        }
    }

    watch(currentPage, async(newPage) => {

        try {
            // setCurrentPage(1);

            const response = await departmentStore.fetchDepartments(currentPage.value, listLimit.value);

            setTotalCount(response.totalCount);
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

    });

    watch(listLimit, async(newListLimit) => {

        try {
            setCurrentPage(1);

            const response = await departmentStore.fetchDepartments(currentPage.value, listLimit.value);

            setTotalCount(response.totalCount);
        } catch (error) {

            const { status, message } = error.response.data;       
            
            if (status === 'DEPARTMENT_NOT_FOUND') {
                alert(message);

            } else if (status === 'REFRESH_TOKEN_INVALID') {
                router.push({name: 'login'});

            } else if (status === 'INTERNAL_SERVER_ERROR') {
                alert('에러가 발생하였습니다.');
            }
        }

    });

    // 현재 라우트를 떠나기 전에 호출된다.
    onBeforeRouteLeave(() => {
        // if (window.confirm('정말 이 페이지를 떠나시겠습니까? 변경 사항이 저장되지 않을 수 있습니다!')) {
        //     return true;
        // } else {
        //     return false;
        // }
        console.log('onBeforeRouteLeave() 호출');
        
        departmentStore.clearState();
    });
    
</script>

<style scoped>

</style>
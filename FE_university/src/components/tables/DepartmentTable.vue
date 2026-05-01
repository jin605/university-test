<template>
    <table class="table table-striped table-hover text-center">
        <thead>
            <tr>
                <th>학과 번호</th>
                <th>학과 이름</th>
                <th>계열</th>
                <th>개설 여부</th>
                <th>정원</th>
                <th>삭제</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="department in departments" :key="department.no"
                @click.stop="emit('itme-click', department.no)">
                <td>{{ department.no }}</td>
                <td>{{ department.name }}</td>
                <td>{{ department.category }}</td>
                <td>{{ department.openYn }}</td>
                <td>{{ department.capacity }}</td>
                <td>
                    <button type="button" class="btn btn-outline-danger"
                        v-if="autoStore.tokenInfo.authorities.includes('ROLE_ADMIN')"
                        @click.stop="emit('delete-click',department.no)">삭제</button>
                </td>
            </tr>
        </tbody>
    </table>
</template>

<script setup>
import { useAuthStore } from '@/stores/authStore';


    const autoStore = useAuthStore();
    
    const props = defineProps({
        departments: {
            type: Array,
            required: true
        }
    });

    const emit = defineEmits(['itme-click', 'delete-click'])
</script>

<style scoped>

</style>